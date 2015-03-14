### Introduction to Mediawiki-JIRA-Connector (MJC) ###
Mediawiki-JIRA-Connector (MJC) consists of a Mediawiki extension and a JIRA plugin. These 2 software will work together to export issues and attachments from JIRA to Mediawiki.

Please go to [screenshots section](http://code.google.com/p/mediawiki-jira-connector/#Screenshots) to see some examples of MJC output.

Users can use JQL query language to select/filter the issues they want to export from JIRA. For more details, please refer to this [JIRA User Guide](http://confluence.atlassian.com/display/JIRA044/Advanced+Searching).

### News ###
  * 28-Dec-2011 - MJC version 1.2 has been released. _**New!**_
  * 18-Dec-2011 - MJC version 1.1 has been released.
  * 15-Dec-2011 - MJC version 1.0 has been released.

### Supported Platform ###
  * JIRA v4.4.1
  * Mediawiki 1.16.0
`**`  MJC may work on other version of JIRA and Mediawiki, but I haven't tested it yet.

### Installation Guide - JIRA Plugin ###
  1. Download the latest MJC zip package from [download page](http://code.google.com/p/mediawiki-jira-connector/downloads/list).
  1. Unzip the MJC zip package.
  1. Verify that there are 3 folders, i.e. `jira-plugin-bin, jira-plugin-src, and mediawiki-extension`.
  1. Verify that there is a jar file in `jira-plugin-bin`, e.g. `mjcjira-jira-rpc-plugin-1.0.jar`.
  1. Login to your JIRA server.
  1. Assuming that your JIRA server's root directory is `/opt/atlassian/jira`, go to `/opt/atlassian/jira/bin/`.
  1. Shutdown JIRA server using the command `./shutdown.sh`.
  1. Verify that JIRA has been shutdown successfully.
  1. Copy the jar file in `jira-plugin-bin` to the following directory: `/opt/atlassian/jira/atlassian-jira/WEB-INF/lib/`.
  1. Go back to this directory: `/opt/atlassian/jira/bin/`.
  1. Start up JIRA server using the command `./startup.sh`.
  1. Verify that JIRA server has been started successfully.
  1. Open a web browser and connect to your JIRA server, e.g. `http://xx.xx.xx.xx:8080/`.
  1. Login to JIRA using admin account.
  1. Click at the button `Administration` to enter administration mode.
  1. Click at the link `Plugin`.
  1. Enter password to gain administrative access, if needed.
  1. Verify that user can see the `Universal Plugin Manager` in JIRA.
  1. Under the tab `Manage Existing`, verify that there are 2 sections, i.e. `User Installed Plugins` and `System Plugins`.
  1. In the `User Installed Plugins` section, verify that user can see the newly installed **`mjcjira-jira-rpc-plugin`**. This is the Mediawiki-JIRA-Connector plugin for Atlassian JIRA. Verify that 2 out of 2 modules in this plugin have been enabled by default.
  1. At the JIRA top menu, go to `System > General Configuration`.
  1. Verify that the parameter `Accept remote API calls` has been set to `ON`.
  1. You have successfully installed MJC JIRA plugin. Please proceed to install MJC Mediawiki extension.

### Installation Guide - Mediawiki Extension ###
  1. Download the latest MJC zip package from [download page](http://code.google.com/p/mediawiki-jira-connector/downloads/list).
  1. Unzip the MJC zip package.
  1. Verify that there are 3 folders, i.e. `jira-plugin-bin, jira-plugin-src, and mediawiki-extension`.
  1. Verify that there is a `.php` file in `mediawiki-extension`, i.e. `mjcmediawiki.php`.
  1. Login to your Mediawiki server.
  1. Assuming that your Mediawiki's root folder is `/var/www/mediawiki/`, please go to this directory: `/var/www/mediawiki/extensions/`. Create a new directory called `MJC`.
  1. Copy the file `mjcmediawiki.php` to `MJC` folder.
  1. Open the file `/var/www/mediawiki/LocalSettings.php` in a text editor.
  1. Add these lines to the bottom of your `LocalSettings.php`:
```
$JIRAs = array(
    'catalina' => array(
        'user' => 'your-jira-admin-username', # Please replace with your JIRA admin username
        'password' => 'your-jira-admin-password', # Please replace with your JIRA admin password
        'urllootest' => 'http://<your-jira-server-ip-address>:8080/rpc/soap/mjcjirasoapservice-v1?wsdl', # Please fill in your JIRA server's IP address
    ),
);
$JIRAdefault = 'catalina';

require_once("$IP/extensions/MJC/mjcmediawiki.php");
```
  1. Open a web browser, go to your JIRA's homepage and login using admin account. Let the web browser save your login password. Search for issues in a project. Select an issues with attachment. Click at the attachment's link. Enter admin password to open the attachment, if needed. Let the web browser save your password.
  1. Now, go to your Mediawiki's homepage.
  1. Create a new Wiki page.
  1. In the Mediawiki editor textbox, add these lines:
```
<jiraexport>
project = "SANDBOX"
</jiraexport>
```
  1. Replace the project Key "SANDBOX" with the project that you want to export from JIRA.
  1. Save the Wiki page.
  1. Verify that MJC is able to export all issues in the selected project to your Wiki page. Verify that all image attachments, e.g. '.png, .jpeg, .jpg, .gif, .bmp' will be shown on your Wiki page. Verify that all non-image attachment such as `.docx, .xlsx, etc` will be shown as a link on your Wiki page.
  1. You have successfully installed MJC Mediawiki extension.

### Screenshots ###
  * Screenshot-1
|<img src='http://mediawiki-jira-connector.googlecode.com/svn/trunk/img/Mediawiki-JIRA-Connector-Screenshot-01.png' width='600' />|
|:--------------------------------------------------------------------------------------------------------------------------------|

  * Screenshot-2
|<img src='http://mediawiki-jira-connector.googlecode.com/svn/trunk/img/Mediawiki-JIRA-Connector-Screenshot-02.png' width='600' />|
|:--------------------------------------------------------------------------------------------------------------------------------|

  * Screenshot-3
|<img src='http://mediawiki-jira-connector.googlecode.com/svn/trunk/img/Mediawiki-JIRA-Connector-Screenshot-03.png' width='600' />|
|:--------------------------------------------------------------------------------------------------------------------------------|

  * Screenshot-4
|<img src='http://mediawiki-jira-connector.googlecode.com/svn/trunk/img/Mediawiki-JIRA-Connector-Screenshot-04.png' width='600' />|
|:--------------------------------------------------------------------------------------------------------------------------------|

  * Screenshot-5
|<img src='http://mediawiki-jira-connector.googlecode.com/svn/trunk/img/Mediawiki-JIRA-Connector-Screenshot-05.png' width='600' />|
|:--------------------------------------------------------------------------------------------------------------------------------|