package code.api.v3_0_0

import code.api.util.APIUtil.OAuth._
import code.api.util.ErrorMessages
import code.api.v1_2_1.{AmountOfMoneyJsonV121 => AmountOfMoneyJSON121}
import code.setup.DefaultUsers
import net.liftweb.json.JsonAST._
import net.liftweb.json.Serialization.write


class AccountTest extends V300ServerSetup with DefaultUsers {
  feature("Assuring that Get all accounts at all banks works as expected - v3.0.0") {

    scenario("We create an account and get accounts as anonymous and then as authenticated user - allAccountsAllBanks") {
      Given("The bank")
      val testBank = mockBankId1

      Then("We create an private account at the bank")
      val accountPutJSON = CreateAccountJsonV300(resourceUser1.userId, "CURRENT", mockAccountLabel1, AmountOfMoneyJSON121("EUR", "0"))
      val requestPut = (v2_0Request / "banks" / testBank.value / "accounts" / mockAccountId1).PUT <@ (user1)
      val responsePut = makePutRequest(requestPut, write(accountPutJSON))

      And("We should get a 200")
      responsePut.code must equal(200)

      When("We make the anonymous access request")
      val requestGet = (v2_0Request / "accounts").GET
      val responseGet = makeGetRequest(requestGet)

      Then("We should get a 200")
      responseGet.code must equal(200)

      val isPublic: List[Boolean] =
        for {
          JObject(o) <- responseGet.body
          JField("is_public", JBool(isPublic)) <- o
        } yield {
          isPublic
        }
      And("All received accounts have to be public")
      isPublic.forall(_ == true) must equal(true)

      When("We make the authenticated access request")
      val requestGetAll = (v2_0Request / "accounts").GET <@ (user1)
      val responseGetAll = makeGetRequest(requestGetAll)

      Then("We should get a 200")
      responseGetAll.code must equal(200)

      val isPublicAll =
        for {
          obj@JObject(o) <- responseGetAll.body
          if (o contains JField("id", JString(mockAccountId1)))
          JBool(isPublic) <- obj \\ "is_public"
        } yield {
          isPublic
        }
      And("The new created account has to be private")
      isPublicAll.forall(_ == false) must equal(true)
    }

    scenario("We create an account and get accounts as anonymous and then as authenticated user - allAccountsAtOneBank") {
      Given("The bank")
      val testBank = mockBankId1

      Then("We create an private account at the bank")
      val accountPutJSON = CreateAccountJsonV300(resourceUser1.userId,"CURRENT", mockAccountLabel1, AmountOfMoneyJSON121("EUR", "0"))
      val requestPut = (v2_0Request / "banks" / testBank.value / "accounts" / mockAccountId1).PUT <@ (user1)
      val responsePut = makePutRequest(requestPut, write(accountPutJSON))

      And("We should get a 200")
      responsePut.code must equal(200)

      When("We make the anonymous access request")
      val requestGet = (v2_0Request / "banks" / testBank.value / "accounts").GET
      val responseGet = makeGetRequest(requestGet)

      Then("We should get a 200")
      responseGet.code must equal(200)

      val isPublic: List[Boolean] =
        for {
          JObject(o) <- responseGet.body
          JField("is_public", JBool(isPublic)) <- o
        } yield {
          isPublic
        }
      And("All received accounts have to be public")
      isPublic.forall(_ == true) must equal(true)

      When("We make the authenticated access request")
      val requestGetAll = (v2_0Request / "banks" / testBank.value / "accounts").GET <@ (user1)
      val responseGetAll = makeGetRequest(requestGetAll)

      Then("We should get a 200")
      responseGetAll.code must equal(200)

      val isPublicAll =
        for {
          obj@JObject(o) <- responseGetAll.body
          if (o contains JField("id", JString(mockAccountId1)))
          JBool(isPublic) <- obj \\ "is_public"
        } yield {
          isPublic
        }
      And("The new created account has to be private")
      isPublicAll.forall(_ == false) must equal(true)
    }

    scenario("We create an account, but with wrong format of account_id ") {
      Given("The bank")
      val testBank = mockBankId1
      val newAccountIdWithSpaces = "account%20with%20spaces"

      Then("We create an private account at the bank")
      val accountPutJSON = CreateAccountJsonV300(resourceUser1.userId, "CURRENT", mockAccountLabel1, AmountOfMoneyJSON121("EUR", "0"))
      val requestPut = (v2_0Request / "banks" / testBank.value / "accounts" / newAccountIdWithSpaces).PUT <@ (user1)
      val responsePut = makePutRequest(requestPut, write(accountPutJSON))

      And("We should get a 400")
      responsePut.code must equal(400)
      And("We should have the error massage")
      val error: String = (responsePut.body \ "error") match {
        case JString(i) => i
        case _ => ""
      }
      Then("We should have the error: " + ErrorMessages.InvalidAccountIdFormat)
      error must equal(ErrorMessages.InvalidAccountIdFormat)
    }
  }

}