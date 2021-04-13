const fs = require('fs');
const path = require('path');

function addProjectLevelDependency(platformRoot) {
  const lib = 'com.google.gms:google-services:4.3.5';
  const dependency = `classpath '${lib}'`;

  const projectBuildFile = path.join(platformRoot, 'build.gradle');

  let fileContents = fs.readFileSync(projectBuildFile, 'utf8');

  const findClassPath = new RegExp(/\bclasspath\b.*/, 'g');
  let matchClassPath = findClassPath.exec(fileContents);
  if (matchClassPath !== null) {
    const checkExistDependency = new RegExp(dependency, 'g');
    let checkMatch = checkExistDependency.exec(fileContents);
    if (checkMatch !== null) {
      console.log('Dependency ' + dependency + ' already exist');
    } else {
      let insertLocation = matchClassPath.index + matchClassPath[0].length;
      fileContents = fileContents.substr(0, insertLocation) + '\n\t\t' + dependency + fileContents.substr(insertLocation);

      fs.writeFileSync(projectBuildFile, fileContents, 'utf8');
      console.log('Updated ' + projectBuildFile + ' to include dependency ' + dependency);
    }
  } else {
    console.error('Unable to insert dependency ' + dependency);
  }
}

module.exports = context => {
  'use strict';
  const platformRoot = path.join(context.opts.projectRoot, 'platforms/android');

  return new Promise((resolve, reject) => {
    addProjectLevelDependency(platformRoot);
    resolve();
  });
};