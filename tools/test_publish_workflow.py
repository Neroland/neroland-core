import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import unittest


WORKFLOW = Path(__file__).resolve().parents[1] / ".github/workflows/publish.yml"
BASH = os.environ.get("NEROLAND_TEST_BASH") or shutil.which("bash")
MOCK_COMMANDS = r"""
unset SECONDS
SECONDS=0

jq() {
  printf 'fabric\t1.20\t\nforge\t1.20\t-all\n'
}
sleep() {
  echo "sleep $1" >> trace
}
curl() {
  case "$PUBLISH_CASE:${!#}" in
    existing:*example-fabric*) printf '200' ;;
    limited_probe:*example-fabric*) printf '429' ;;
    *) printf '404' ;;
  esac
}
mvn() {
  local attempts arg
  read -r attempts < attempts
  attempts=$((attempts + 1))
  echo "$attempts" > attempts
  for arg in "$@"; do
    if [[ "$arg" == -Dfile=* ]]; then
      echo "deploy ${arg#-Dfile=}" >> trace
    fi
  done
  case "$PUBLISH_CASE:$attempts" in
    limited_once:1|limited_always:*)
      echo "status code: 429, reason phrase: Too Many Requests"
      return 1
      ;;
    unauthorized:1)
      echo "status code: 403, reason phrase: Forbidden"
      return 1
      ;;
  esac
}
"""


@unittest.skipUnless(BASH, "Bash is required to test the release workflow")
class PublishWorkflowTest(unittest.TestCase):
    def test_paces_uploads_and_retries_rate_limits(self):
        workflow = WORKFLOW.read_text(encoding="utf-8")
        match = re.search(
            r"(?ms)^      - name: Publish Maven artifacts to GitHub Packages\n"
            r".*?^        run: \|\n((?:^ {10}[^\n]*\n|^\n)+)",
            workflow,
        )
        self.assertIsNotNone(match)
        script = MOCK_COMMANDS + "\n".join(
            line[10:] for line in match.group(1).splitlines()
        ) + "\n"
        cases = (
            ("ok", 0, ["fabric", "forge"], [2]),
            ("existing", 0, ["forge"], []),
            ("limited_once", 0, ["fabric", "fabric", "forge"], [60, 2]),
            ("limited_always", 1, ["fabric"] * 4, [60, 120, 240]),
            ("unauthorized", 1, ["fabric"], []),
            ("limited_probe", 1, [], []),
        )
        for name, exit_code, deployments, waits in cases:
            with self.subTest(name=name), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                (root / "gradle.properties").write_bytes(
                    b"mod_id=example\nmod_group_id=za.co.test.example\n"
                )
                (root / "attempts").write_bytes(b"0\n")
                (root / "trace").touch()
                environment = dict(
                    os.environ,
                    PUBLISH_CASE=name,
                    GITHUB_ACTOR="test",
                    GITHUB_TOKEN="test",
                    GITHUB_REPOSITORY="test/repo",
                    MATRIX="{}",
                    VERSION="1",
                    RUNNER_TEMP=root.as_posix(),
                )
                result = subprocess.run(
                    [BASH],
                    input=script.encode(),
                    cwd=root,
                    env=environment,
                    capture_output=True,
                    check=False,
                    timeout=15,
                )
                self.assertEqual(result.returncode, exit_code, result.stdout + result.stderr)
                trace = (root / "trace").read_text(encoding="utf-8").splitlines()
                expected_files = {
                    "fabric": "built/build-fabric-1.20/dist/example-fabric-1.20-1.jar",
                    "forge": "built/build-forge-1.20/maven.jar",
                }
                self.assertEqual(
                    [line.removeprefix("deploy ") for line in trace if line.startswith("deploy ")],
                    [expected_files[loader] for loader in deployments],
                )
                self.assertEqual(
                    [int(line.split()[1]) for line in trace if line.startswith("sleep ")],
                    waits,
                )


if __name__ == "__main__":
    unittest.main()
