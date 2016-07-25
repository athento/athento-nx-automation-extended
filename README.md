# athento-nx-automation-extended

## For versions
6.0

## Download
You can download [latest release from here](https://github.com/athento/athento-nx-automation-extended/raw/master/athento-nx-automation-extended-dist/target/athento-nx-automation-extended-project-2.5.zip)

## Synopsis

This project adds some useful operations and features for automation chains. You can run custom or standard operations before document creations or update. You can also affect a query by executing a custom operation on it.

## Configuration
Here you can find some tips to configure your plugins. 
![Extended Config example](/screenshots/automation-extended-config.png)

**This configuration features depends on athento-nx-extended-config plugin located [here](https://github.com/athento/athento-nx-extended-config)**

## Available operations
### RunOperationAsWorker
![operation RunOperationAsWorker](/screenshots/runOperationAsWorker.png)

### Athento.DocumentCreate
Allows you to execute any operation before document creation. See [config section](#configuration)
### Athento.DocumentUpdate
Allows you to execute any operation before document update. You can also set the docTypes affected by this feature. See [config section](#configuration)
### Athento.DocumentDelete
Deletes a document.
### Athento.DocumentResultSet
Allows you to execute any operation before querying the system for some documents. You can also set the docTypes affected by this feature. See [config section](#configuration)

### Athento.Exception.Create
Creates a custom exception for every operation encapsulating root cause as message.
#### Uncatched exception 500
![Internal Server Error](/screenshots/500_Unexpected_exception.png)
#### Catched exception 400
![Bad request](/screenshots/400_bad_request.png)


## Installation
1. Download the zip package from   [here](https://github.com/athento/athento-nx-automation-extended/automation-extended-dist/target/athento-nx-automation-extended-project-1.9.zip)
2. Install from the ADMIN section > Update Center > local packages
3. Restart the server and enjoy your new feature.

Don't forget to report any bug you find. Pull requests are also welcomed, feel free to improve :)
