package dao

import org.scalatest.WordSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.MustMatchers
import org.apache.commons.dbcp.BasicDataSource
import java.sql.Connection
import org.hittepit.smapapi.transaction.TransactionManager
import model.BookType

class TestBookDao extends WordSpec with BeforeAndAfter with MustMatchers{
 class DataSource extends BasicDataSource {
    this.setDriverClassName("org.h2.Driver")
    this.setUsername("h2")
    this.defaultAutoCommit = false
    this.defaultTransactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    this.setUrl("jdbc:h2:mem:test;MVCC=TRUE")
  }

  val ds = new DataSource

  val bookDao = new BookDao(TransactionManager(ds))
  
  before {
    val connection = ds.getConnection()
    var st = connection.createStatement
    st.addBatch("create table BOOK (ID integer auto_increment, TITLE varchar(50), ISBN VARCHAR(13), AUTHOR varchar(50), BOOK_TYPE varchar(5), PRIMARY KEY(id));")
    st.addBatch("insert into BOOK (id,title,isbn,author,book_type) values (1000,'Dune','2940199612','Frank Herbert','eBook');")
    st.executeBatch()
    connection.commit()
    connection.close
  }

  after {
    val connection = ds.getConnection()
    var st = connection.createStatement
    st.addBatch("delete from BOOK");
    st.addBatch("drop table BOOK;")
    st.executeBatch()
    connection.commit()
    connection.close
  }

  "The find method" when {
    "called with an existing id" must {
      "return an option with the correct Book object" in {
        val b = bookDao.find(1000)
        b must be('defined)
        val book = b.get
        book.id must be (Some(1000))
        book.title must be("Dune")
        book.author must be (Some("Frank Herbert"))
        book.isbn must be("2940199612")
        book.bookType must be(BookType.eBook)
      }
    }
    "called with an non-existing id" must {
      "return None" in {
        val b = bookDao.find(100000)
        b must be(None)
      }
    }
  }
}