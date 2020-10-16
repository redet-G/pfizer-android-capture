import requests
import json
import os

print("Updating pull request labels")

GITHUB_RELEASE_API_TOKEN = os.environ.get('GITHUB_RELEASE_API_TOKEN')

# Get all OPEN PR
opened_prs = requests.get("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls?state=open&access_token=%s" %(GITHUB_RELEASE_API_TOKEN)).json()
for pr in opened_prs:
    print(pr['number'])
    labels_to_add = []
    has_reviews = requests.get("https://api.github.com/repos/dhis2/dhis2-android-capture-app/pulls/%s/reviews?access_token=%s" %(pr['number'], GITHUB_RELEASE_API_TOKEN)).json()

    #Select label for PR type
    labelType = ""
    if pr['title'].find("build") == 0:
        labelType = "enhancement"
    elif pr['title'].find('ci') == 0:
        labelType = "enhancement"
    elif pr['title'].find('docs') == 0:
        labelType = "Documentation"
    elif pr['title'].find('feat') == 0:
        labelType = "enhancement"
    elif pr['title'].find('fix') == 0:
        labelType = "bug"
    elif pr['title'].find('perf') == 0:
        labelType = "refactor"
    elif pr['title'].find('refactor') == 0:
        labelType = "refactor"
    elif pr['title'].find('style') == 0:
        labelType = "design"
    elif pr['title'].find('test') == 0:
        labelType = "Unit Test"
    else:
        labelType = ""

    if labelType != "":
        labels_to_add.append(labelType)

    #Select label for issue state
    jira_issue_init_pos = pr['title'].find("[") + 1
    jira_issue_end_pos = pr['title'].find("]")

    if jira_issue_init_pos-1 != jira_issue_end_pos:

        jira_issue_label = pr['title'][jira_issue_init_pos: jira_issue_end_pos]

        print('Checking jira issue: %s' % jira_issue_init_pos)

        jira_issue = requests.get('https://jira.dhis2.org/rest/api/latest/issue/%s' %(jira_issue_label)).json()
        print(jira_issue)
        status = jira_issue['fields']['status']['name']
        jira_assignee = jira_issue['fields']['assignee']['name']

        labelState = ""
        if status == 'In Review':
            if has_reviews and len(pr['requested_reviewers']) > 0:
                labelState = 'Waiting for approval'
            elif has_reviews and len(pr['requested_reviewers']) == 0:
                labelState = 'Ready for testing'
            else:
                labelState = 'Waiting for approval'
        elif status == 'Testing' or status == 'Retesting':
            if jira_assignee == 'nancyespinoza' or jira_assignee == "fgomez011" or jira_assignee == "":
                labelState = 'Tested'
        elif status == 'Needs Update' or status == 'Reopen':
            labelState = 'changes requested'
        else:
            labelState = ""

        if labelType != "":
            labels_to_add.append(labelState)
    else:
        labelState = ""
        if has_reviews and len(pr['requested_reviewers']) > 0:
            labelState = 'Waiting for approval'
        elif has_reviews and len(pr['requested_reviewers']) == 0:
            labelState = 'Ready for testing'
        else:
            labelState = 'Waiting for approval'
        if labelType != "":
            labels_to_add.append(labelState)

    print(str(labels_to_add)[1:-1])
    payload = '{"labels":%s}' % json.dumps(labels_to_add)
    requests.post("https://api.github.com/repos/dhis2/dhis2-android-capture-app/issues/%s/labels?access_token=%s" %(pr['number'], GITHUB_RELEASE_API_TOKEN), data = payload)