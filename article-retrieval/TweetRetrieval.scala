import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import scala.collection.JavaConversions._
import com.github.nscala_time.time.Imports._
 
object TweetRetrieval { 
	val consumerKey = "zNlV1WdlTzhmeoRGGSuAUF0SI"
	val consumerSecret = "5sWRHmv3HRsCpuI4VYNrVD0SH5GCuVTY4hUc9NEVw4dP5HyjWa"
  def main(args : Array[String]) {
		// Build a configuration to fetch the authentication token.
		val builder = new ConfigurationBuilder()
			.setApplicationOnlyAuthEnabled(true)
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
    val token = new TwitterFactory(builder.build()).getInstance().getOAuth2Token()

		// Build a configuration and corresponding Twitter object to use.
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
			.setApplicationOnlyAuthEnabled(true)
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
			.setOAuth2TokenType(token.getTokenType())
			.setOAuth2AccessToken(token.getAccessToken())
    val tf = new TwitterFactory(cb.build())
    val twitter = tf.getInstance()
 
    // Use the twitter object to fetch and print some tweets.
		val query = new Query("Google")
		query.setCount(20)
    val result = twitter.search(query)
    val tweets = result.getTweets();
		for (tweet <- tweets) {
				val date = new DateTime(tweet.getCreatedAt()).toString()
				println(date + " @" + tweet.getUser().getScreenName() + " - " + tweet.getText());
		}
  }
}