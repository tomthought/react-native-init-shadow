# react-native-init-shadow

Initialize shadow-cljs projects to target react-native. This project
uses the existing `react-native init` cli to generate the project's
base, and will generate the project using the latest stable
react-native version.

## Usage

shadow-cljs and react-native must already be installed.

You can run directly from terminal using leiningen, from within the
react-native-init-shadow project directory.

```shell
$ lein run MyAwesomeProject
```

This will generate your project within the react-native-init-shadow
directory.

Once the project is setup, you can test it out by running the
following, assuming terminal on MacOS.

```shell
$ cd my-awesome-project
$ npm install
$ shadow-cljs watch dev
$ npx react-native run-ios # or npx react-native run-android
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
