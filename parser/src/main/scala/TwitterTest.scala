object TwitterTest {
	def main(args : Array[String]) {
		    // some runnable test code
		    val query = "google"
		    val tweetData = TwitterSearch.getTweets(query)
		    var i = 0
		    for (datum <- tweetData) {
		    	try {
		    		println("\n[Tweet] "+datum.getText())
		    		val (sentiment,popularity) = TweetProcessor.processTweet(datum,query);
		    		println("["+i+"] "+sentiment+" "+popularity);
		    	} catch {
		    		case e : Exception => println("\nTweet couldn't be parsed");
		    	}
			}
	}
}