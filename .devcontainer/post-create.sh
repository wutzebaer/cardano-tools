#!/bin/bash
set -e

sudo chown -R vscode:vscode /home/vscode

# Add ll alias (ls -al)
echo 'alias ll="ls -al"' >> ~/.bashrc
echo 'alias mvn=mvnd' >> ~/.bashrc
source ~/.bashrc

# Compile the application
./mvn clean compile

wget -O runonsave.vsix https://emeraldwalk.gallerycdn.vsassets.io/extensions/emeraldwalk/runonsave/1.1.5/1767933106225/Microsoft.VisualStudio.Services.VSIXPackage
cursor --install-extension runonsave.vsix