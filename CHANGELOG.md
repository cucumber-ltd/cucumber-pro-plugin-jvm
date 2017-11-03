# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

## [1.1.1] - 2017-09-03

### Added

* Added `CUCUMBER_PRO_IGNORE_CONNECTION_ERROR` to prevent build failures (#4).
* Added `CUCUMBER_PRO_CONNECTION_TIMEOUT` to set custom connection timeout.
* Plugin suggests defining `CUCUMBER_PRO_TOKEN` on auth failure (#6).
* Added a changelog.

### Fixed

* Don't fail when `CUCUMBER_PRO_GIT_HOST_KEY` isn't specified (#10).

## 1.1.0 - 2017-09-02

First proper release!

[Unreleased]: https://github.com/cucumber-ltd/cucumber-pro-plugin-jvm/compare/v1.1.1...HEAD
[1.1.1]: https://github.com/cucumber-ltd/cucumber-pro-plugin-jvm/compare/v1.1.0...1.1.1