package model

object BookType extends Enumeration{
  type BookType = Value
  
  val eBook = Value("eBook")
  val paper = Value("paper")
}

import BookType._

class Book(val id:Option[Int], val title:String, val isbn:String, val author:Option[String], val bookType:BookType)