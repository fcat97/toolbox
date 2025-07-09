#!/usr/bin/env bash

# git-utils.sh -- A collection of useful git utilities

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'

function compare_branches() {
    echo -e "${CYAN}Fetching all branches from origin...${NC}"
    git fetch origin --prune

    echo -e "${CYAN}Comparing local and remote branches:${NC}"
    branches=$(git for-each-ref --format='%(refname:short)' refs/heads/)

    for branch in $branches; do
        remote_branch="origin/$branch"
        if git show-ref --verify --quiet "refs/remotes/$remote_branch"; then
            ahead=$(git rev-list --left-right --count "$branch...$remote_branch" | awk '{print $1}')
            behind=$(git rev-list --left-right --count "$branch...$remote_branch" | awk '{print $2}')
            if [[ "$ahead" -eq 0 && "$behind" -eq 0 ]]; then
                printf "${GREEN}%s${NC} ${CYAN}===${NC} ${MAGENTA}%s${NC}\n" "$branch" "$remote_branch"
            else
                printf "${YELLOW}%s${NC} [%s↑ | %s↓] ${MAGENTA}%s${NC}\n" \
                    "$branch" \
                    "$( [[ $ahead -ne 0 ]] && echo -e "${RED}$ahead${YELLOW}" || echo "0")" \
                    "$( [[ $behind -ne 0 ]] && echo -e "${RED}$behind${YELLOW}" || echo "0")" \
                    "$remote_branch"
            fi
        else
            printf "${RED}%s${NC} [local only]\n" "$branch"
        fi
    done
}

function clean_local_branches() {
    echo -e "${CYAN}Fetching all branches from origin...${NC}"
    git fetch origin --prune

    current_branch=$(git branch --show-current)
    branches=$(git for-each-ref --format='%(refname:short)' refs/heads/)
    deleted_any=0

    for branch in $branches; do
        if [[ "$branch" == "$current_branch" ]]; then
            continue
        fi
        remote_branch="origin/$branch"
        if git show-ref --verify --quiet "refs/remotes/$remote_branch"; then
            ahead=$(git rev-list --left-right --count "$branch...$remote_branch" | awk '{print $1}')
            behind=$(git rev-list --left-right --count "$branch...$remote_branch" | awk '{print $2}')
            if [[ "$ahead" -eq 0 && "$behind" -eq 0 ]]; then
                git branch -d "$branch"
                echo -e "${GREEN}Deleted synced branch:${NC} $branch"
                deleted_any=1
            fi
        fi
    done

    if [[ "$deleted_any" -eq 0 ]]; then
        echo -e "${YELLOW}No fully-synced local branches to delete.${NC}"
    fi
}

function select_branch_menu() {
    echo -e "${CYAN}Fetching all branches...${NC}"
    git fetch --all --prune > /dev/null 2>&1

    mapfile -t all_branches < <(
        git for-each-ref --format='%(refname:short)' refs/heads/ \
        && git for-each-ref --format='%(refname:short)' refs/remotes/origin/ \
        | grep -vE '^(origin/HEAD|HEAD$)' \
        | sort | uniq
    )

    printf "\n${BOLD}${CYAN}%-5s %-35s${NC}\n" "No." "Branch"
    printf "${BOLD}${CYAN}------------------------------------------${NC}\n"
    for i in "${!all_branches[@]}"; do
        printf "${YELLOW}%-5s${NC} %-35s\n" "$((i+1))" "${all_branches[$i]}"
    done
    printf "${BOLD}${CYAN}------------------------------------------${NC}\n"

    declare -gA BRANCH_INDEX_MAP
    for i in "${!all_branches[@]}"; do
        BRANCH_INDEX_MAP["${all_branches[$i]}"]=$((i+1))
    done

    while true; do
        read -p "Select a branch by number (or q to quit): " branch_num
        if [[ "$branch_num" == "q" || "$branch_num" == "Q" ]]; then
            return 1
        elif [[ "$branch_num" =~ ^[0-9]+$ && "$branch_num" -ge 1 && "$branch_num" -le "${#all_branches[@]}" ]]; then
            selected_branch="${all_branches[$((branch_num-1))]}"
            return 0
        else
            echo -e "${RED}Invalid selection. Try again.${NC}"
        fi
    done
}

function list_merged_branches() {
    if ! select_branch_menu; then
        return
    fi
    echo -e "\n${CYAN}Listing branches merged into ${MAGENTA}$selected_branch${NC}\n"

    if git show-ref --verify --quiet "refs/heads/$selected_branch"; then
        base_branch="$selected_branch"
    elif git show-ref --verify --quiet "refs/remotes/$selected_branch"; then
        base_branch="remotes/$selected_branch"
    else
        echo -e "${RED}Branch reference not found: $selected_branch${NC}"
        return
    fi

    mapfile -t local_merged < <(git branch --merged "$base_branch" | grep -vE "^\*|\b$selected_branch$" | sed 's/^[ *]*//')
    declare -A commit_to_branches
    for branch in "${local_merged[@]}"; do
        commit=$(git rev-parse "$branch")
        commit_to_branches["$commit"]+="$branch,"
    done

    if [[ ${#commit_to_branches[@]} -eq 0 ]]; then
        echo -e "${YELLOW}No local branches are fully merged into ${MAGENTA}$selected_branch${NC}"
    else
        echo -e "${GREEN}Local branches fully merged into ${MAGENTA}$selected_branch${NC}:"
        for commit in "${!commit_to_branches[@]}"; do
            branches_line="${commit_to_branches[$commit]%,}"
            IFS=',' read -ra branches_array <<< "$branches_line"
            for idx in "${!branches_array[@]}"; do
                branch_name="${branches_array[$idx]}"
                branch_num="${BRANCH_INDEX_MAP[$branch_name]}"
                if [[ $idx -eq 0 ]]; then
                    echo    "-| $branch_num | $branch_name"
                else
                    echo -e "\t-| $branch_num | $branch_name"
                fi
            done
        done
    fi

    mapfile -t remote_merged < <(git branch -r --merged "$base_branch" | grep '^  origin/' | grep -v "$selected_branch" | grep -v "origin/HEAD" | sed 's/^[ *]*//')
    declare -A remote_commit_to_branches
    for branch in "${remote_merged[@]}"; do
        commit=$(git rev-parse "$branch")
        remote_commit_to_branches["$commit"]+="$branch,"
    done

    if [[ ${#remote_commit_to_branches[@]} -gt 0 ]]; then
        echo -e "\n${GREEN}Remote (origin) branches merged into ${MAGENTA}$selected_branch${NC}:"
        for commit in "${!remote_commit_to_branches[@]}"; do
            branches_line="${remote_commit_to_branches[$commit]%,}"
            IFS=',' read -ra branches_array <<< "$branches_line"
            for idx in "${!branches_array[@]}"; do
                branch_name="${branches_array[$idx]}"
                branch_num="${BRANCH_INDEX_MAP[$branch_name]}"
                if [[ $idx -eq 0 ]]; then
                    echo    "-| $branch_num | $branch_name"
                else
                    echo -e "\t-| $branch_num | $branch_name"
                fi
            done
        done
    fi
}

function push_local_only_branches() {
    echo -e "${CYAN}Fetching all branches from origin...${NC}"
    git fetch origin --prune

    echo -e "${CYAN}Pushing all local-only branches to remote...${NC}"
    current_branch=$(git branch --show-current)
    branches=$(git for-each-ref --format='%(refname:short)' refs/heads/)
    pushed_any=0

    for branch in $branches; do
        remote_branch="origin/$branch"
        if ! git show-ref --verify --quiet "refs/remotes/$remote_branch"; then
            echo -e "${YELLOW}Pushing branch:${NC} $branch"
            if git push -u origin "$branch"; then
                echo -e "${GREEN}Successfully pushed:${NC} $branch"
                pushed_any=1
            else
                echo -e "${RED}Failed to push:${NC} $branch"
            fi
        fi
    done

    if [[ "$pushed_any" -eq 0 ]]; then
        echo -e "${YELLOW}No local-only branches to push.${NC}"
    fi
}

function compare_with_selected_branch() {
    if ! select_branch_menu; then
        return
    fi
    echo -e "\n${CYAN}Comparing ${MAGENTA}$selected_branch${NC} with all other branches\n"

    # Build a sorted list of all branches except the selected one
    mapfile -t other_branches < <(
        (git for-each-ref --format='%(refname:short)' refs/heads/
        git for-each-ref --format='%(refname:short)' refs/remotes/origin/
        ) | grep -vE '^(origin/HEAD|HEAD$)' | grep -vFx "$selected_branch" | sort | uniq
    )

    for branch in "${other_branches[@]}"; do
        [[ -z "$branch" ]] && continue
        [[ "$branch" == "$selected_branch" ]] && continue

        ahead=$(git rev-list --left-right --count "$selected_branch...$branch" 2>/dev/null | awk '{print $1}')
        behind=$(git rev-list --left-right --count "$selected_branch...$branch" 2>/dev/null | awk '{print $2}')

        if [[ "$ahead" -ne 0 || "$behind" -ne 0 ]]; then
            printf "${YELLOW}%s${NC} [%s↑ | %s↓] ${MAGENTA}%s${NC}\n" \
                "$selected_branch" \
                "$( [[ $ahead -ne 0 ]] && echo -e "${RED}$ahead${YELLOW}" || echo "0")" \
                "$( [[ $behind -ne 0 ]] && echo -e "${RED}$behind${YELLOW}" || echo "0")" \
                "$branch"
        fi
    done
}

function show_menu() {
    echo -e "${CYAN}Git Utils - Select a feature to run:${NC}"
    echo "1) Compare local and remote branches"
    echo "2) Clean (delete) local branches fully synced with remote"
    echo "3) List branches merged in a selected branch"
    echo "4) Push all local-only branches to remote"
    echo "5) Compare a selected branch with all other branches"
    echo "q) Quit"
    echo
    read -p "Enter your choice [1-5/q]: " choice
}

while true; do
    show_menu
    case "$choice" in
        1)
            compare_branches
            ;;
        2)
            clean_local_branches
            ;;
        3)
            list_merged_branches
            ;;
        4)
            push_local_only_branches
            ;;
        5)
            compare_with_selected_branch
            ;;
        q|Q)
            echo "Bye!"
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid choice. Please try again.${NC}"
            ;;
    esac
    echo
done
