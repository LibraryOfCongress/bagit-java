#!/usr/bin/env bash
#
# Copyright (C) 2016 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

REMOTE="https://@github.com/${GITHUB_REPOSITORY}"
git remote set-url origin ${REMOTE}

pip install -r .github/workflows/mkdocs/requirements.txt

echo "START deploying docs to GitHub pages..."
mkdocs gh-deploy --force
echo "DONE deploying docs to GitHub pages."
