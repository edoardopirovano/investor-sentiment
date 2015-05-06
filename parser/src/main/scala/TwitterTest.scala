// runnable code to test the Tweet fetching and processing modules
object TwitterTest {
	def main(args : Array[String]) {
		    // some runnable test code
		    val query = "Google"
		    val tweetData = TwitterSearch.getTweets(query)
		    var i = 0
		    for (datum <- tweetData) {
		    	try {
		    		val (sentiment,popularity) = TweetProcessor.processTweet(datum,query);
		    		println("["+i+"] "+sentiment+" "+popularity);
		    	} catch {
		    		case e : Exception => println("Tweet couldn't be parsed");
		    	}
		    }
	 	}
}