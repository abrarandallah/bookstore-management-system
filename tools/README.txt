One-time helper scripts used while translating the books into Arabic and French.
They are NOT part of the running app - Docker never copies this folder into the image.

Each script's header explains what it does. The commands written in those headers
assume the script sits in the project root, so now that they live here, add "tools/"
in front of the script name, for example:

    ... python -u tools/translate_books.py

The .sql files are checks you can paste into a MySQL client.
