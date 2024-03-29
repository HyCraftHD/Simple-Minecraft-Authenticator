# Changelog
All notable changes to this project will be documented in this file.

## [2.2.3] - 2023-09-14
### Changed
 - Update minecraft authenticator version to 3.0.6 to fix a module bug with gson

## [2.2.2] - 2023-06-17
### Changed
 - Move future stuff to util class for usage outside of this library

## [2.2.1] - 2023-06-17
### Changed
 - Expose executor

## [2.2.0] - 2023-04-28
### Changed
 - Cancel future for inital authentication now, when outside thread is interrupted and always call finishInitalAuthentication to free up resources

## [2.1.1] - 2023-04-19
### Changed
 - Update to 3.0.5 minecraft authenticator to include login state callbacks

## [2.1.0] - 2023-04-17
### Changed
 - Remove unused interfaces

## [2.0.0] - 2023-03-04
### Changed
 - Rewrite web authentication method with jdk.httpserver
 - Update minecraft authentication to 3.0.4
 - Rewrite publishing to publish library and cli version

## [1.0.2] - 2022-02-24
### Changed
 - Adjusted parameters for main function
 - Generalize extra properties

## [1.0.1] - 2022-02-24
### Changed
 - Update minecraft authenticator dependency to 3.0.2

## [1.0.0] - 2022-02-24
### Added
 - Added two simple minecraft authenticator methods. Console and web