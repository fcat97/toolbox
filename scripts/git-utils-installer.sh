#!/bin/bash

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[1;36m'
RED='\033[0;31m'
RESET='\033[0m'

TOOLBOX_DIR="$HOME/.fcat97/toolbox/scripts"
SCRIPT_URL="https://raw.githubusercontent.com/fcat97/toolbox/909f2f8cd3bd2649be584df4f3171e4e2501e2ba/scripts/git-utils.sh"
SCRIPT_NAME="git-utils.sh"

echo -e "${CYAN}Installing git-utils.sh...${RESET}"

# Create directory
if mkdir -p "$TOOLBOX_DIR"; then
  echo -e "${GREEN}Created directory:$TOOLBOX_DIR${RESET}"
else
  echo -e "${RED}Failed to create directory: $TOOLBOX_DIR${RESET}"
  exit 1
fi

# Download the git-utils.sh script
if curl -fsSL "$SCRIPT_URL" -o "$TOOLBOX_DIR/$SCRIPT_NAME"; then
  echo -e "${GREEN}Downloaded git-utils.sh${RESET}"
else
  echo -e "${RED}Failed to download script from $SCRIPT_URL${RESET}"
  exit 1
fi

# Make it executable
if chmod +x "$TOOLBOX_DIR/$SCRIPT_NAME"; then
  echo -e "${GREEN}Made git-utils.sh executable${RESET}"
else
  echo -e "${RED}Failed to set executable permission${RESET}"
  exit 1
fi

# Add to PATH if not already present (for bash and zsh)
for profile in "$HOME/.bashrc" "$HOME/.zshrc"; do
  if [ -f "$profile" ]; then
    if grep -q 'export PATH="$HOME/.fcat97/toolbox/scripts:$PATH"' "$profile"; then
      echo -e "${YELLOW}PATH already set in $profile${RESET}"
    else
      echo 'export PATH="$HOME/.fcat97/toolbox/scripts:$PATH"' >> "$profile"
      echo -e "${GREEN}Added toolbox to PATH in $profile${RESET}"
    fi
  fi
done

echo -e "${CYAN}Installation complete!${RESET}"
echo -e "${YELLOW}Run 'source ~/.bashrc' or 'source ~/.zshrc', or restart your terminal to update your PATH.${RESET}"

echo -e "${CYAN}Cleaning up installer script...${RESET}"
rm -- "$0"
