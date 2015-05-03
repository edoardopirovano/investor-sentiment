import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
 
object Controller {

  def main(args : Array[String]) {
		while(true) {
			println("Beginning refresh of data.")
			// To be filled in once data fetching classes are made.
			println("Refreshing daily scores.")
			Scorer.main(Array[String]())
			println("Data refresh complete.")
			wait(3600)
		}
	}
	
	def wait(seconds: Int) {
			val f = Future { Thread.sleep(seconds*1000) }
			Await.result(f, Duration.Inf)
	}
	
}