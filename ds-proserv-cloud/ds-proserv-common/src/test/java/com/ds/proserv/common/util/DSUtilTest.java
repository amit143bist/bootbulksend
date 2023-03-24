package com.ds.proserv.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@ActiveProfiles(value = "unittest")
@Slf4j
class DSUtilTest {

    @Test
    void buildRoleTabLabelMapHappyPath() {
        String CorrectHeader = "landlord::communitypartnercode,landlord::communitypartnername,landlord::languagepreference,tenant::languagepreference,landlord::lemail,landlord::lfullname,tenant::tfirstname,tenant::tmiddlename,tenant::tlastname,tenant::tsuffix,landlord::temail,landlord::tfullname,landlord::isstandardized,landlord::taddrline1,landlord::taddrline2,landlord::taddrline3,landlord::taddrcity,landlord::taddrstate,landlord::taddrzipcode,landlord::taddrcounty,landlord::tsaddrline1,landlord::tsaddrline2,landlord::tsaddrline3,landlord::tsaddrcity,landlord::tsaddrstate,landlord::tsaddrzipcode,landlord::tsaddrcounty,landlord::laid2020received,landlord::tenantneedera,landlord::monthlyrentamount,landlord::totalmonthspastdue,landlord::totalrentpastdue,landlord::may2021pastdue,landlord::jun2021pastdue,landlord::jul2021pastdue,landlord::dec2020pastdue,landlord::nov2020pastdue,landlord::jan2021pastdue,landlord::feb2021pastdue,landlord::mar2021pastdue,landlord::apr2021pastdue,landlord::jun2020pastdue,landlord::may2020pastdue,landlord::jul2020pastdue,landlord::aug2020pastdue,landlord::sep2020pastdue,landlord::oct2020pastdue,landlord::totalprospectiverent,landlord::totalrppgrant";
        Map<String, Map<String,String>> rolesMaps = DSUtil.buildRoleTabLabelMap(CorrectHeader);
        log.info("rolesMaps -> {}", rolesMaps);
        Assert.assertNotNull(rolesMaps);
        Assert.assertTrue("landlord key was not found", rolesMaps.containsKey("landlord"));
        Assert.assertEquals(rolesMaps.get("landlord").size(), 44);
        Assert.assertEquals(rolesMaps.get("tenant").size(), 5);
        Assert.assertEquals(2, rolesMaps.size());
    }

    @Test
    void getRoleName() {

        String col = "landlord::columnName";
        Assert.assertEquals(DSUtil.getRoleName(col), "landlord");
        col = "columnName";
        Assert.assertNull(DSUtil.getRoleName(col));
    }

    @Test
    void getDataLabel() {
        String col = "landlord::columnName";
        Assert.assertEquals(DSUtil.getDataLabel(col), "columnName");
        col = "columnName";
        Assert.assertEquals(DSUtil.getDataLabel(col), "columnName");
    }
}