package dao

import org.hittepit.smapapi.transaction.JdbcTransaction
import org.hittepit.smapapi.transaction.TransactionManager
import org.slf4j.LoggerFactory
import org.hittepit.smapapi.core.result.Row
import org.hittepit.smapapi.core.NotNullableString
import model.BookType
import org.hittepit.smapapi.core.NullableInt
import model.Book
import org.hittepit.smapapi.core.NullableString
import org.hittepit.smapapi.core.Param
import org.hittepit.smapapi.core.NotNullableInt

class BookDao(val transactionManager:TransactionManager) extends JdbcTransaction{
	val logger = LoggerFactory.getLogger(classOf[BookDao])
	
	def bookMapper(row:Row) = {
	  val bookType = BookType.withName(row.getColumnValue("BOOK_TYPE", NotNullableString))
	  new Book(row.getColumnValue("ID", NullableInt), 
	      row.getColumnValue("TITLE", NotNullableString), 
	      row.getColumnValue("ISBN", NotNullableString), 
	      row.getColumnValue("AUTHOR", NullableString), 
	      bookType)
	}
	
	def find(id:Int) = readOnly{session =>
	  session.unique("select * from BOOK where id=?", List(Param(id,NotNullableInt)), bookMapper)
	}
	
	def findAll = readOnly{session =>
	  session.select("select * from Book",List()) map bookMapper
	}
}