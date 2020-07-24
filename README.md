# react-native-init-shadow

Initialize shadow-cljs projects to target react-native. This project
uses the existing `react-native-init` cli to generate the project's
base, and will generate the project using the latest stable
react-native version.

## Usage

shadow-cljs and react-native must already be installed.

```shell
$ npx react-native-init-shadow MyProjectName
```

Options 

```shell
# Install a specific react-native version. Note that
# react-native-windows may not work with the latest RN.
$ npx react-native-init-shadow MyProjectName --version 0.62.2
```

```shell
# Generate a desktop (windows + macos) project, or a mobile (ios +
# android) project.
$ npx react-native-init-shadow MyProjectName --platform desktop|mobile
```

```shell
# Set the package name for Android
$ npx react-native-init-shadow MyProjectName --package com.mycompany.MyProjectName
```

## License

Copyright © 2020 Tom Goldsmith

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
