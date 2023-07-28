# Social Network - NovaLite Backend Internship

## Brief overview

This project demonstrates how a backend of a barebone social network is supposed to look.

The basic structure is there:
1. Homepage that has a list of all posts of the user and his friends
2. Authentication through login and registration
3. Creating posts
4. Liking posts
5. Friend requests and a friend system
6. Commenting on other user's posts

## Authentication through login and registration
Authentication was the first thing I made. The process involves two endpoints

`POST localhost:9000/register`

`POST localhost:9000/login`

First endpoint is used when a user hasn't created an account yet. 

For both login and registration, the user is asked to enter a JSON in the form of

````json
{
  "username": "wanted username",
  "password": "wanted password"  
}
````

After registration, the password is stored in the database, **hashed**. The user is given a [JWT token](https://en.wikipedia.org/wiki/JSON_Web_Token).

All authorization afterwards is done throgh the JWT token.

## Creating posts
After successfully inserting a JWT token, the user can choose to create a post.

`POST localhost:9000/post/make`

After entering the data in the form of a JSON, user has created a post successfully.:
```json
{
  "title": "wanted title",
  "caption": "wanted caption"  
}
```

`POST localhost:9000/post/:id/delete`
Only the owner of the post can delete that same post.

`GET     localhost:9000/post/:id/read`
Returns a JSON of the post.

## Liking posts
After successfully inserting a JWT token, the user can choose to like a post.

Liking a post is only possible if that user is friends with the owner of the post (or is owner themselves).

`POST    localhost:9000/post/:id/like`

`POST    localhost:9000/post/:id/dislike`

Two endpoints that require an ID of the post in order to like them.

User can only like a post once, or dislike an already liked post.

Number of likes is 0 in the beginning, going up for how many people like it.

## Friend requests and a friend system
Each user has an option to add friends. Friends must be users that are registered, and that exist.

`GET     localhost:9000/user/friends` returns a JSON array that has all friends of the user.

`POST    localhost:9000/friendrequest/send` is a way to send friend request to another user. 
That user receives it and can choose whether to accept or deny it. It requires a JSON:

`POST    localhost:9000/friendrequest/delete` is used for a user to delete a friend request that had been sent previously.
This only works if the friend request is "pending", not accepted yet.

`POST    localhost:9000/friendrequest/accept` is used to accept a friend request on another user's end. 
They have to be logged in.

`POST    localhost:9000/friendrequest/decline` is used to decline a friend request on another user's end.
They have to be logged in.

send, delete, accept and decline endpoints require a JSON in the form of:
```json
{
  "username": "username of the wanted friend"
}
```

## Commenting on posts
`POST    localhost:9000/post/comment` is used to create a comment on a post. 

The user needs to be either the owner of the post or friends with the owner of the post.

It requires a JSON in the form of:
```json
{
  "idOfPost": 1,
  "commentText": "content of the comment"
}
```

`POST    localhost:9000/post/:postid/comment/all/remove`
is used to remove all comments on a post.

`POST    localhost:9000/post/:postid/comment/:commentid/remove`
is used to remove a comment with *commentid* on *postid*. Only the owner of the comment can remove the comment.


## Homepage that has a list of all posts of the user and their friends
`GET localhost:9000/` is a home page that lists all posts in a JSON object.

It has all likes, ids, comments, and an indicator whether the user liked the post or not.


## Technologies used
**Java 1.8, Scala, SBT, play framework, slick framework, MySQL**