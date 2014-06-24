package dao

import org.hittepit.smapapi.transaction.JdbcTransaction
import org.hittepit.smapapi.transaction.TransactionManager
import org.slf4j.LoggerFactory
import org.hittepit.smapapi.core.result.Row
import org.hittepit.smapapi.core.StringProperty
import model.BookType
import org.hittepit.smapapi.core.OptionalIntProperty
import model.Book
import org.hittepit.smapapi.core.OptionalStringProperty
import org.hittepit.smapapi.core.Param
import org.hittepit.smapapi.core.IntProperty
import model.Isbn
import org.hittepit.smapapi.core.Column

class BookDao(val transactionManager:TransactionManager) extends JdbcTransaction{
	val logger = LoggerFactory.getLogger(classOf[BookDao])

	def bookMapper(row:Row) = {
	  val bookType = BookType.withName(row.getColumnValue("BOOK_TYPE", StringProperty))
	  new Book(row.getColumnValue("ID", OptionalIntProperty), 
	      row.getColumnValue("TITLE", StringProperty), 
	      Isbn(row.getColumnValue("ISBN", StringProperty)), 
	      row.getColumnValue("AUTHOR", OptionalStringProperty), 
	      bookType)
	}

	def find(id:Int) = readOnly{session =>
	  session.unique("select * from BOOK where id=?", List(Param(id,IntProperty)), bookMapper)
	}

	def findAll = readOnly{session =>
	  session.select("select * from Book",List()) map bookMapper
	}

	def saveOrUpdate(book:Book) = inTransaction{session =>
	  book.id match {
	  	case None => 
	  	  val id = session.insert("insert into book (title,isbn,author,book_type) values (?,?,?,?)", 
	  	    List(Param(book.title,StringProperty),Param(book.isbn.code,StringProperty),Param(book.author,OptionalStringProperty),Param(book.bookType.toString,StringProperty)), 
	  	    Column("id",IntProperty))
	  	  id match {
	  	  	case Some(i) => new Book(Some(i),book.title,book.isbn,book.author,book.bookType)
	  	  	case None => throw new Exception("No id generated")
	  	  }
	  	  
	  	case Some(id) => 
	  	  session.execute("update book set title=?, isbn=?, author=?, book_type=? where id=?",
	  	      List(Param(book.title,StringProperty),
	  	          Param(book.isbn.code,StringProperty),
	  	          Param(book.author,OptionalStringProperty),
	  	          Param(book.bookType.toString,StringProperty),
	  	          Param(book.id.get,IntProperty)))
	  	  book
	  }
	}
	
	def delete(book:Book) = inTransaction{session =>
	  book.id match {
	    case Some(id) => session.execute("delete book where id=?",List(Param(id,IntProperty)))
	    case None => throw new Exception("Transient book")
	  }
	}
}