VITAM TOOLS-MAILEXTRACT
=======================

This project implement a local or distant mail box extraction application (class MailExtract) and the really operational class (class StoreExtractor) that can be used elsewhere.
It uses JavaMail library for distant imap(s) and local Thunderbird extraction, and java-libpst for local Outlook extraction (thanks to  to Richard Johnson http://github.com/rjohnsondev).
It's a standard maven project (quickstart archetype) with full javadoc (in /doc)

You can see under the manual for the application (and see what the StoreExtractor class can do...)

Launching
---------

* Linux or windows

java -Dfile.encoding="UTF-8" -jar mailextract.jar arguments...
*The tools have always to be launch in UTF-8 mode to maintain consistency of exports and imports*

* Windows

Use the mailextract.exe available in the "windows" directory(created with launch4j) in a "cmd" window

install_dir\mailextract arguments...

MailExtract manual
------------------

It performs extraction and structure listing of mail boxes from different sources:

  * *IMAP* or *IMAPS* server with user/password login
  * *Thunderbird directory* containing mbox files and .sbd directory hierarchy
  * *Outlook pst* file

The extraction generate on disk a directories/files structure convenient for SEDA archive packet (NF Z44-022). For detailed information see class StoreExtractor.

The operation, extraction or listing, can be logged on console and file (root/username[-timestamp].log - cf args). At the different levels you can have: extraction errors (SEVERE), warning about extraction problems and items dropped (WARNING), information about global process (INFO), list of treated folders (FINE), list of treated messages (FINER), problems with some expected metadata (FINEST).
The default level of log is INFO for extracting and OFF for listing.

The arguments syntax is:

--help              help
--mailprotocol      mail protocol for server access (imap|imaps...)
--thunderbird       thunderbird mbox directory
--outlook           outlook pst file
--user              user account name (also used for destination extraction naming)
--password          password
--server            mail server [HostName|IP](:port)
--container         mail container directory for mbox or file for pst
--folder            specific mail folder used as root for extraction or listing
--rootdir           root (default current directory) for output to root/username directory
--addtimestamp      add a timestamp to output directory (root/username-timestamp)
--keeponlydeep      keep only empty folders not at root level
--dropemptyfolders  drop empty folders
--loglevel          event level to log (SEVERE| WARNING| INFO| FINE| FINER| FINEST)
--namesshortened    generate short directories and files names
--warning           generate warning when there's a problem on a message (otherwise log at FINEST level)
-l                  access account and list folders (no drop options)
-z                  access account and list folders and there statistics (no drop options)

Long options can be reduced to short ones (for example -h is equivalent to --help)

**Warning**: Listing with detailed information is a potentially expensive operation, especially when accessing distant account, as all messages are inspected (in the case of a distant account that mean also downloaded...).

Note: For now it can't extract S/MIME (ciphered and/or signed) messages.
