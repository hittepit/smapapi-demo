package model

import org.scalatest.BeforeAndAfter
import org.scalatest.WordSpec
import org.scalatest.MustMatchers

class TestIsbn extends WordSpec with BeforeAndAfter with MustMatchers{
	"Isbn Value Object" when {
	  "created with a correct 13-digit code" must {
	    "be instanciated" in {
	      Isbn("9780450011849")
	    }
	    "return the 13 digit value" in {
	      val v = Isbn("9780450011849")
	      v.code must be("9780450011849")
	    }
	  }
	  "created with a correct 10-digit code" must {
	    "be instanciated" in {
	      Isbn("2253071951")
	    }
	    "return the 13 digit converted value" in {
	      val v = Isbn("2266111566")
	      v.code must be("9782266111560")
	    }
	  }
	  "created with a incorrect code" must {
	    "throw an IllegalArgumentException" in {
	      an [IllegalArgumentException] must be thrownBy(Isbn("1234567890"))
	    }
	  }
	}
}