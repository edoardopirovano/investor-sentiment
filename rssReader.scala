/**
 * Created by Octavian on 03/05/2015.
 */

import scala.xml.XML
import scala.xml.Elem

object rssReader {
  //val link = "http://feeds.finance.yahoo.com/rss/2.0/headline?s=appl,ford,msft,ea,fdx,%20gm,%20goog,%20gps,gs,nke,yhoo&region=US&lang=en-US"
  private val xmlfile = new String
  private var stocks : List[String]= List()

/*
  method for generating the rss feed link
   */
  private def link : String ={
    var s = "http://finance.yahoo.com/rss/headline?s="

    if(stocks.size == 0) throw error("You haven't added any stocks to request rss feed for");

    val i = stocks.iterator

    while(i.hasNext){
      s = s ++ i.next() ++ ","
    }

    s.dropRight(1)
  }

  def getRSSfeed(source : String) :  Elem={
    val xml = XML.load(source);
    xml
  }

/*
###### methods for choosing which stocks to get rss feed for
 */

  /*
  Get stock symbol from here: http://finance.yahoo.com/lookup
   */
  def addStock(symbol : String): Unit ={
    stocks = symbol :: stocks
  }

  private def remove(symbol: String, list: List[String]) = list diff List(symbol)

  def removeStock(symbol : String) : Unit = {
    remove(symbol, stocks)
  }

  def deleteAllStocks = {
    stocks = List()
  }
  /*
  #############################################################################################################

  Te method that extract the required information from the feed
  Compatible only with Yahoo Finance News feed
   */

  type Item = (String, String, String)

  def extractor (xml : scala.xml.Elem) : List[Item]={
    val items = (xml \\ "item") // preserve only the nodes with the items
    val dates = (items \ "pubDate").map(_.text) // extract date from each item
    val titles = (items \ "title").map(_.text) // extract title from each item
    var links = (items \ "link").map(_.text) // extract link from each item

    // they are extracted in order, therefore the only error (and "misspairing" can occur if
    // not all dates, titles or links are read
    assert(dates.size == titles.size && dates.size == links.size)

    /*
    There are 2 links provided, separated by a star

    Example:
    ~~~~~~~~
    http://us.rd.yahoo.com/finance/external/xengadget/rss/SIG=11roh08b5*http://www.engadget.com/2015/05/02/build-2015-windows-10/?ncid=rss_truncated
    is changed to
    http://www.engadget.com/2015/05/02/build-2015-windows-10/?ncid=rss_truncated
     */
    links = links.map(_.split("\\*").last)

    // zip the 3 lists
    val result = (titles, links, dates).zipped.toList

    // return the thing
    result
  }

  def main(args: Array[String]) {

    stocks = List("appl","ford","msft","ea","fdx", "gm", "goog", "gps","gs","nke","yhoo") ::: stocks

    //getting the xml file
    val xmlfile = getRSSfeed(link)
   // println(xmlfile)

    //extracting the information required from the rss feed (title, link, and date of each article
    val things = extractor(xmlfile)

    // testing the output
    val i = things.iterator

    while(i.hasNext) {
      val (title, link, date) = i.next
      println(title)
      println("       "+ link)
      println("       "+ date)
    }
  }
}
