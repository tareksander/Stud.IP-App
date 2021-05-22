# Stud.IP-App
![](https://img.shields.io/github/license/tareksander/Stud.IP-App)  
![](https://img.shields.io/f-droid/v/org.studip.unofficial_app.svg)  
![](https://img.shields.io/github/v/release/tareksander/Stud.IP-App?include_prereleases)

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75">](https://f-droid.org/packages/org.studip.unofficial_app/)

An unofficial app for Stud.IP systems.

[Deutsche Beschreibung](https://raw.githubusercontent.com/tareksander/Stud.IP-App/master/fastlane/metadata/android/de/full_description.txt)  
[English description](https://raw.githubusercontent.com/tareksander/Stud.IP-App/master/fastlane/metadata/android/en-US/full_description.txt)
</br>
</br>
</br>
</br>
</br>
</br>
Required API routes by features:  
</br>
</br>
Requirement for the app to work at all:  
  
/discovery  
/user  
/user/:user_id  
</br>
</br>
Files screen:  
  
/file/:file_ref_id/move/:destination_folder_id  
/file/:file_ref_id/copy/:destination_folder_id  
/file/:file_ref_id/download  
/file/:file_ref_id/update  
/file/:file_ref_id (GET, PUT, DELETE, POST)  
/folder/:folder_id (GET, PUT, DELETE  
/folder/:folder_id/subfolders  
/folder/:folder_id/files  
/folder/:parent_folder_id/new_folder  
/folder/:source/copy/:destination  
/folder/:source/move/:destination  
/course/:course_id/top_folder (for viewing course files)  
/user/:user_id/top_folder (for viewing your own files)  
/studip/content_terms_of_use_list  
/studip/file_system/folder_types  
</br>
</br>
Courses screen:
  
/course/:course_id  
/course/:course_id/members  
/course/:course_id/news  
/user/:user_id/courses  
/semesters  
/semester/:semester_id  
news/:news_id/comments (GET, PUT)  
</br>
</br>
Messages screen:  
  
/user/:user_id/contacts  
/user/:user_id/contacts/:friend_id (PUT, DELETE)  
/user/:user_id/:box  
/messages  
/messages/:message_id ( DELETE, GET, PUT)  
/message/:message_id/file_folder  
</br>
</br>
Home screen:  
  
/studip/news  
/news/:news_id  
</br>
</br>
Planner screen:  
  
/course/:course_id/events  
/user/:user_id/events  
/user/:user_id/schedule  
/user/:user_id/schedule/:semester_id  
</br>
</br>
Blubber:  
  
/course/:course_id/blubber (GET, POST)  
/blubber/comment/:blubber_id (DELETE, GET, PUT)  
/blubber/posting/:blubber_id (DELETE, GET, PUT)  
/blubber/posting/:blubber_id/comments (GET, POST)  
/blubber/postings
/blubber/stream/:stream_id
</br>
</br>
Course forum:  
  
/course/:course_id/forum_categories  
/forum_category/:category_id  
/forum_category/:category_id/areas (GET, POST)  
/forum_entry/:entry_id (DELETE, GET, POST, PUT)  


