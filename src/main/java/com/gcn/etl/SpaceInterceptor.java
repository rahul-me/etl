package com.gcn.etl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcn.etl.database.models.Space;
import com.gcn.etl.database.service.SpaceService;
import com.gcn.etl.helper.ETLErrors;
import com.gcn.etl.pojo.Errors;
import com.gcn.etl.pojo.Message;

@Component
public class SpaceInterceptor implements HandlerInterceptor {
	private static Logger logger = LogManager.getLogger(SpaceInterceptor.class.getName());

	@Autowired
	SpaceService spaceService;

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean preHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2) throws Exception {
		Map pathVariables = (Map) arg0.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String spaceId = (String) pathVariables.get("spaceId");

		Space space = spaceService.findByGeneratedUUID(spaceId);
		if (space == null) {
			Map<String, Object> map = new HashMap<>();
			map.put("requestId", "");
			map.put("status", "Failed");
			map.put("Timestamp", new Date().toString());
			Errors errors = new Errors();
			errors.setApplicationErrorCode(ETLErrors.SPACE_NOT_FOUND_GENERAL_ERROR.errorCode());
			errors.setApplicationErrorMsg(ETLErrors.SPACE_NOT_FOUND_GENERAL_ERROR.errorMessage());
			map.put("errors", errors);
			
			ObjectMapper mapper = new ObjectMapper();
			arg1.setContentType("application/json");
			arg1.setStatus(HttpServletResponse.SC_NOT_FOUND);
			arg1.getWriter().write(mapper.writeValueAsString(map));

			return false;
		}
		logger.info("this is interceptor, preHandle method");
		return true;
	}

}
