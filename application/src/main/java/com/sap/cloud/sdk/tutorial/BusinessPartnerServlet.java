package com.sap.cloud.sdk.tutorial;

import com.google.gson.Gson;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestinationProperties;
import com.sap.cloud.sdk.datamodel.odata.helper.FluentHelperRead;
import com.sap.cloud.sdk.datamodel.odata.helper.Order;
import com.sap.cloud.sdk.odatav2.connectivity.ODataException;
import com.sap.cloud.sdk.s4hana.connectivity.DefaultErpHttpDestination;
import com.sap.cloud.sdk.s4hana.connectivity.ErpHttpDestination;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.BusinessPartnerService;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * This servlet is for local env, BusinessPartnerCFServlet is for SCP CF deployment.
 * It also works for cloud env.
 * There are some slight difference in using the destination for API key manipulation.
 */
@WebServlet("/businesspartners")
public class BusinessPartnerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(BusinessPartnerServlet.class);

    private static final String CATEGORY_PERSON = "1";

//    private final ErpHttpDestination destination = DestinationAccessor.getDestination("SAPAPIHub").asHttp().decorate(DefaultErpHttpDestination::new);

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            //use builder to build a destination, in order to put APIKey in it.
            //The default means in the tutorial doesnt work for SAP Hub API.
            DefaultErpHttpDestination dehd = DefaultErpHttpDestination.builder("https://sandbox.api.sap.com/s4hanacloud").header("APIKey","ONPzwpOGR1GkWpwXSZ9ggcFQ3p2rxFK0").build();
            final List<BusinessPartner> businessPartners = new DefaultBusinessPartnerService().getAllBusinessPartner()
                            .select(BusinessPartner.BUSINESS_PARTNER,
                                    BusinessPartner.LAST_NAME,
                                    BusinessPartner.FIRST_NAME,
                                    BusinessPartner.IS_MALE,
                                    BusinessPartner.IS_FEMALE,
                                    BusinessPartner.CREATION_DATE)
                            .filter(BusinessPartner.BUSINESS_PARTNER_CATEGORY.eq(CATEGORY_PERSON))
                            .orderBy(BusinessPartner.LAST_NAME, Order.ASC)//
                            .top(200).execute(dehd);
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(businessPartners));
        } catch (final ODataException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
        }
    }
}