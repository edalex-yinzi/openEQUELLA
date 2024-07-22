package com.tle.webtests.test.importexport;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.institution.ImportTab;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import com.tle.webtests.pageobject.institution.ServerAdminLogonPage;
import com.tle.webtests.pageobject.institution.StatusPage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ImportTest extends AbstractInstTest {

  @Override
  protected void prepareBrowserSession() {
    InstitutionListTab listTab =
        new ServerAdminLogonPage(context)
            .load()
            .logon(testConfig.getAdminPassword(), new InstitutionListTab(context));
  }

  @Test(dataProvider = "toImport")
  public void importInstitutions(File instFolder, String fileName) {
    String shortName = instFolder.getName();
    String instutionUrl = context.getBaseUrl() + shortName + '/';
    InstitutionListTab listTab = new InstitutionListTab(context).load();
    ImportTab importTab = listTab.importTab();
    if (listTab.institutionExists(instutionUrl)) {
      StatusPage<InstitutionListTab> statusPage = listTab.delete(instutionUrl);
      assertTrue(statusPage.waitForFinish());
      statusPage.back();
    }
    assertTrue(
        importTab
            .importInstitution(instutionUrl, shortName, new File(instFolder, fileName), 360)
            .waitForFinish());
  }

  @Test(
      dependsOnMethods = {"importInstitutions"},
      dataProvider = "toImport",
      alwaysRun = true)
  public void deleteInstitutions(File instFolder, String fileName) {
    String shortName = instFolder.getName();
    String instutionUrl = context.getBaseUrl() + shortName + '/';
    InstitutionListTab listTab = null;

    listTab = new InstitutionListTab(context).load();
    if (listTab.institutionExists(instutionUrl)) {
      StatusPage<InstitutionListTab> statusPage = listTab.delete(instutionUrl);
      assertTrue(statusPage.waitForFinish());
      statusPage.back();
    }
    listTab.importTab();
  }

  @DataProvider(parallel = false)
  public Object[][] toImport() throws Exception {
    File[] institutions =
        new File(testConfig.getTestFolder(), "tests/importexport/institutions").listFiles();
    List<Object[]> instDirs = new ArrayList<Object[]>();
    for (File instDir : institutions) {
      File[] listFiles = instDir.listFiles();
      if (listFiles.length > 0) {
        String fileName = listFiles[0].getName();
        if (fileName.endsWith(".gz")
            || fileName.endsWith(".tgz")
            || fileName.endsWith(".bz2")
            || fileName.endsWith(".zip")) {
          instDirs.add(new Object[] {instDir, fileName});
        }
      }
    }
    return instDirs.toArray(new Object[instDirs.size()][]);
  }
}
