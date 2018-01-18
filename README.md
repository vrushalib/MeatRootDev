MeatRootDev : Upgrade to 8.7
===========

- Upgrade procedure impacts DB & codebase
- DB upgrade has a very well defined process as long as we don't alter, modify existing tables in any way. so following the execution of all the upgrade scripts from version 7.4 to 8.7 suffice.
- Codebase upgrade is slightly tricky as some of the existing code has been modified for UI functionalities and bit for API functionalities.



We can have 2 approaches to upgrade the codebase as follows:

1. Get the latest codebase and merge business specific code changes into it.
2. Identify what are the upgraded code piece from latest version and merge into our codebase.

Approach 1 would be much easier as we know or would be easier to identify what functionality addition we have done, than to identify new functional code changes in new code base, as per approach 2.

