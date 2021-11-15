package com.tle.webtests.pageobject.selection;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SelectionStatusPage extends AbstractPage<SelectionStatusPage> {
  @FindBy(id = "ss_finishedButton")
  private WebElement finishedButton;

  @FindBy(id = "selection-summary")
  private WebElement summaryDiv;

  public SelectionStatusPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return summaryDiv;
  }

  public SelectionCheckoutPage finishSelections() {
    ExpectWaiter.waiter(ExpectedConditions.visibilityOf(finishedButton), this).get();
    scrollToElement(finishedButton);
    finishedButton.click();
    return new SelectionCheckoutPage(context).get();
  }
}
