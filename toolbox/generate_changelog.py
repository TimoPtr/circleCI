import getopt
import os
import sys
import re

GITHUB_PR_NUMBER = re.compile('\(#[0-9]+\)')
JIRA_KEY = re.compile('(feature|bugfix)?[/ ]?KLTB002[- ][0-9]+', re.IGNORECASE)

def printUsage():
    print("usage: generate_changelog.py --from=[commit_hash|tag|branch] [--to=[commit_hash|tag|branch]] <path(s)>")
    return

def printChangelog(logFrom, logTo, paths):
    commits = os.popen("git log --pretty=%s " + logFrom + ".." + logTo + " -- " + " ".join(paths))
    for commit in commits:
        commitMessage = commit.rstrip()
        
        pullRequestMatch = GITHUB_PR_NUMBER.search(commitMessage)
        jiraTicketMatch = JIRA_KEY.search(commitMessage)

        if jiraTicketMatch != None:
            commitMessage = commitMessage.replace(jiraTicketMatch.group(), "").strip()    
        if pullRequestMatch != None:
            commitMessage = commitMessage.replace(pullRequestMatch.group(), "").strip()
            # TODO integrate GitHub SDK to retrieve data if PR number is available
            # TODO integrate Jira SDK to get more information about the associated ticked if it is available in GitHub
            # TODO group changes based on ticket type
            # TODO add jira ticket link as markdown
        print("- " + commitMessage)
    return

def main(argv):
    try:
        optlist, paths = getopt.gnu_getopt(
            argv[1:],
            '',
            ["from=", "to="])
    except getopt.GetoptError:
        printUsage()
        return 2

    if len(paths) == 0:
        printUsage()
        return 2

    opts = dict(optlist)

    if len(opts) == 0:
        printUsage()
        return 2

    if "--from" not in opts:
        printUsage()
        return 2

    logFrom = opts["--from"]

    if "--to" in opts:
        logTo = opts["--to"]
    else:
        logTo = "HEAD"

    print("Changelog between '" + logFrom + "' and '" + logTo + "':\n")
    printChangelog(logFrom, logTo, paths)

    return 0

if __name__ == '__main__':
    sys.exit(main(sys.argv))
