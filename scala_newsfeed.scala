package com.example

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }



object Producer
{

  def props(server: ActorRef): Props = Props(new Producer(server))

  final case class SendPost(category: String, post: String)

  final case class SubscribeP(where: String)

  final case class PrintSelfP()

}

class Producer(server: ActorRef) extends Actor 
{

  import Producer._
  import Server._

  def receive = {

    case SendPost(category, post) =>
      server ! NewPost(category, post)

 	case SubscribeP(category) =>
      server ! ServerSubscribeP(category)

    case PrintSelfP() =>
    	 println("I am \n" + self)
  }
}






// CONSUMER
object Consumer 
{
  def props(server: ActorRef): Props = Props(new Consumer(server))

  final case class SubscribeC(where: String)

  final case class ReceivePost(post: String)

  final case class PrintSelfC()

}

class Consumer(server: ActorRef) extends Actor 
{
  import Consumer._
  import Server._
  
  def receive = {

    case SubscribeC(category) =>
      server ! ServerSubscribeC(category)

    case PrintSelfC() =>
    	 println("I am \n" + self)

    case ReceivePost(post) =>
    	println("(" + self + ") received : " + post)

    case "test" =>
    	println("( test : " + self + ")")
  }
}



class myRef(myactor: ActorRef) extends Actor{

	import Consumer._
	import Producer._

	var cat_list : List[String] = List() 

	def receive = {
		case "test" =>
    	println("( test : " + self + ")")
	}
}


object Server 
{
  def props: Props = Props[Server]

  final case class NewPost(category: String, post: String)

  final case class ServerSubscribeC(category: String)
  final case class ServerSubscribeP(category: String)

}

class Server extends Actor with ActorLogging 
{
  	import scala.util.control._
  	import Server._
  	import Array._
	import Consumer._
	import Producer._

  // Categories
  	var array_tech_consumers : List[ActorRef] = List()

  	var array_tech_producers : List[ActorRef] = List()

  	var array_weather_consumers : List[ActorRef] = List()

  	var array_weather_producers : List[ActorRef] = List()

  	var array_sports_consumers : List[ActorRef] = List()

  	var array_sports_producers : List[ActorRef] = List()


  	def receive = 

	{

		case NewPost(category, post) =>

	    	category match {

	    		case "TECH" =>
	    		
	    			if( array_tech_producers contains sender() ){

	    				array_tech_consumers.foreach { _ ! ReceivePost(post) }

	    			}else{
	    				println("Cannot post to unsubscribed category")
	    			}

	    		case "WEATHER" => 

	    			if( array_weather_producers contains sender() ){

	    				array_weather_consumers.foreach { _ ! ReceivePost(post) }

	    			}else{
	    				println("Cannot post to unsubscribed category")
	    			}


	    		case "SPORTS" =>

	    			if( array_sports_producers contains sender() ){

	    				array_sports_consumers.foreach { _ ! ReceivePost(post) }

	    			}else{
	    				println("Cannot post to unsubscribed category")
	    			}

				case whoa => println("Unexpected case: " + whoa.toString)
	    	}


	    case ServerSubscribeC(category) =>

	    	category match {


	    		case "TECH" => 
	    			if(! (array_tech_consumers contains sender() ))
	    				array_tech_consumers = List(sender()) ::: array_tech_consumers

	      		case "WEATHER" => 
	      			if(! (array_weather_consumers contains sender() ))
	      				array_weather_consumers = List(sender()) ::: array_weather_consumers

		        case "SPORTS" => 
		        	if(! (array_sports_consumers contains sender() ))
		        		array_sports_consumers = List(sender()) ::: array_sports_consumers

		        case whoa => println("Unexpected case: " + whoa.toString)
	    	}

	    case ServerSubscribeP(category) =>

	    	category match {


	    		case "TECH" => 
	    			if(! (array_tech_producers contains sender()  ))
	    				println("peosp " + sender())
	    				array_tech_producers = List(sender()) ::: array_tech_producers

	      		case "WEATHER" => 
	      			if(! (array_weather_producers contains sender() ))
	      			array_weather_producers = List(sender()) ::: array_weather_producers

		        case "SPORTS" => 
		        	if(! (array_sports_producers contains sender() ))
		        	array_sports_producers = List(sender()) ::: array_sports_producers

		        case whoa => println("Unexpected case: " + whoa.toString)
	    	}

  	}//end of receive
}






object AkkaQuickstart extends App 
{

	import Consumer._

	import Producer._

	val system: ActorSystem = ActorSystem("System")

	val server: ActorRef = system.actorOf(Server.props, "server")

  	val producer: ActorRef = system.actorOf(Producer.props(server), "prodA")

  	val consumerA: ActorRef = system.actorOf(Consumer.props(server), "consA")

	val consumerB: ActorRef = system.actorOf(Consumer.props(server), "consB")


	consumerA ! SubscribeC("TECH")

	consumerB ! SubscribeC("TECH")

	consumerB ! SubscribeC("WEATHER")

	producer ! SubscribeP("TECH")

	producer ! SubscribeP("WEATHER")

	producer ! SendPost("TECH","tech news");

	producer ! SendPost("WEATHER","weather news");

	producer ! SendPost("SPORTS","tech news");

}