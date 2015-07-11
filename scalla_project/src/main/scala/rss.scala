import twitter4j.TwitterFactory
import twitter4j.Twitter
import twitter4j.conf.ConfigurationBuilder

import scala.io.Source
import scala.xml.{NodeSeq, XML}
import akka.actor._
import scala.concurrent.duration._
import akka.dispatch._
import scala.concurrent.ExecutionContext._

object PostingActor {
  case object Start
  case object Post
}
class PostingActor (val system: ActorSystem) extends Actor {
  import PostingActor._
  import context._
  val config = new twitter4j.conf.ConfigurationBuilder()
    .setOAuthConsumerKey("cFAtCOAvBdhzS6RMi22tcT5Uo")
    .setOAuthConsumerSecret("GysdmAaSji645w7zsVeyjnkiLjE4abbgSccuCNdLExFs8Z6ifH")
    .setOAuthAccessToken("3288729670-FNZc3eEu5lC3V7Gr4v2eSwgWOiwoOfeSdxr7ECW")
    .setOAuthAccessTokenSecret("3GfAdPUhHJBVwm5wGh6oGKYOExsSaVy1GgEC4q5U56NzE")

  val tf = new TwitterFactory(config.build())
  val twitter = tf.getInstance()

  var category = new String

  val statuses = twitter.getHomeTimeline()
  val it = statuses.iterator()
  while (it.hasNext()) {
    val status = it.next()
    if (status.getText().contains(":rss ")) {
    category = status.getText().replace(":rss ", "")
    }
  }

  val preparedCategory = category.replace(" ", "+")
  println(preparedCategory)
  val address = "http://www.reddit.com/search.rss?q=" + preparedCategory + "&sort=top&t=hour"


  def receive = {
    case Start => println("AA")

      context.system.scheduler.schedule(0 minute, 61 minute, self, Post)

    case Post => println ("BB")

      val xml = XML.load(address)
      val links = (xml \\ "rss" \\ "channel" \\ "item")
      links.foreach { n =>
        val link = (n \\ "link").text
        val description = (n \\ "title").text
        twitter.updateStatus(link)


      }
  }
}

object rss extends App {
  import PostingActor._

  val system = ActorSystem("PostingSystem")
  val postingActor = system.actorOf(Props(new PostingActor(system)), name = "postingactor")
  postingActor ! Start
}
