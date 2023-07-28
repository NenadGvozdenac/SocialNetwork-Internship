package services

import dtos.{PostWithLike, PostsWithLikes, PostsWithLikesAndComments}
import exceptions.{PostCouldNotBeCreatedException, PostCouldNotBeEditedException, PostCouldNotBeFoundException}
import models.{Comment, Post}
import repositories.{CommentRepository, FriendRequestRepository, LikeRepository, PostRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostService @Inject() (repository: PostRepository,
                             likeRepository: LikeRepository,
                             commentRepository: CommentRepository,
                             postRepository: PostRepository,
                             friendRequestRepository: FriendRequestRepository) (implicit ex: ExecutionContext) {

  def createPost(title: String, caption: String, username: String) : Future[Int] = {
    if(title != "" && caption != "") {
      repository.createNewPost(new Post(title, caption, username))
    } else {
      Future.failed(new PostCouldNotBeCreatedException)
    }
  }

  def readAllPosts(): Future[Seq[Post]] = repository.readAllPosts()

  def readPostsByUsername(username: String): Future[Seq[Post]] = repository.readPostsByUsername(username)

  def readPostById(id: Int): Future[Option[Post]] = repository.readPostById(id)

  def editPost(postId: Int, newPost: Future[Post]): Future[Int] = {
    repository.readPostById(postId).flatMap {
      case Some(value) => repository.editPost(postId, newPost)
      case _ => Future.successful(0)
    }
  }
  def deletePost(id: Int, username: String): Future[Int] = {
    repository.readPostById(id).flatMap {
      case Some(value) =>
        if(value.usernameOfPoster == username) {
          likeRepository.deleteAllLikesWithPostID(id)
          commentRepository.deleteAllCommentsWithPostID(id)
          repository.deletePost(id)
        } else Future.failed(new PostCouldNotBeEditedException("wrong username"))
      case _ => Future.failed(new PostCouldNotBeFoundException)
    }
  }

  def readAllPostsFromFriends(username: String): Future[PostsWithLikesAndComments] = {
    friendRequestRepository.listFriends(username).flatMap { friendRequests =>
      val friendsUsernames = friendRequests.map { friendRequest =>
        if (friendRequest.usernameFrom == username) {
          friendRequest.usernameTo
        } else {
          friendRequest.usernameFrom
        }
      }

      postRepository.readAllPostsInAscendingOrderFromFriends(username, friendsUsernames).flatMap { posts =>
        val postIds = posts.map(_.id)

        val userLikesMapFutures: Future[Seq[(Int, Boolean)]] = Future.traverse(postIds) { postId =>
          likeRepository.checkIfUserLikedThePost(postId, username).map {
            case Some(like) => postId -> true
            case None => postId -> false
          }
        }

        val commentsMapFutures: Future[Seq[(Int, Seq[Comment])]] = Future.traverse(postIds) { postId =>
          commentRepository.getCommentsForPost(postId).map { comments =>
            postId -> comments
          }
        }

        for {
          userLikesMap <- userLikesMapFutures
          commentsMap <- commentsMapFutures
        } yield {
          val commentsMapByPostId = commentsMap.toMap
          val postsWithLikes = posts.map { post =>
            val liked = userLikesMap.find { case (postId, _) => postId == post.id }.exists(_._2)
            val comments = commentsMapByPostId.getOrElse(post.id, Seq.empty)
            new PostWithLike(post, liked, comments)
          }
          PostsWithLikesAndComments(postsWithLikes)
        }
      }
    }
  }

//
//  def readAllPostsFromFriends(username: String): Future[PostsWithLikes] = {
//    friendRequestRepository.listFriends(username).flatMap { friendRequests =>
//      val friendsUsernames = friendRequests.map { friendRequest =>
//        if (friendRequest.usernameFrom == username) {
//          friendRequest.usernameTo
//        } else {
//          friendRequest.usernameFrom
//        }
//      }
//
//      postRepository.readAllPostsInAscendingOrderFromFriends(username, friendsUsernames).flatMap { posts =>
//        val postIds = posts.map(_.id)
//
//        val userLikesMapFutures: Future[Seq[(Int, Boolean)]] = Future.traverse(postIds) { postId =>
//          likeRepository.checkIfUserLikedThePost(postId, username).map {
//            case Some(like) => postId -> true
//            case None => postId -> false
//          }
//        }
//
//        userLikesMapFutures.map { userLikesMap =>
//          val postsWithLikes = posts.map { post =>
//            val liked = userLikesMap.find { case (postId, _) => postId == post.id }.map(_._2).getOrElse(false)
//            new PostWithLike(post, liked)
//          }
//          PostsWithLikes(postsWithLikes)
//        }
//      }
//    }
//  }
}