package com.cetera.ng.web.lr.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cetera.ng.web.lr.constant.CeteraURIConstant;
import com.cetera.ng.workflow.lr.constant.CeteraConstants;
import com.cetera.ng.workflow.lr.entity.PageModuleBDGMapping;
import com.cetera.ng.workflow.lr.entity.UserRegistrationPageSubmit;
import com.cetera.ng.workflow.lr.entity.Users;
import com.cetera.ng.workflow.lr.exceptions.CustomGenericException;
import com.cetera.ng.workflow.lr.exceptions.ErrorProperties;
import com.cetera.ng.workflow.lr.service.FeesService;
import com.cetera.ng.workflow.lr.service.RegistrationService;
import com.cetera.ng.workflow.lr.service.UsersDetailService;
import com.cetera.ng.workflow.lr.util.CommonUtils;
import com.cetera.ng.workflow.lr.util.ServiceUtil;
import com.cetera.ng.workflow.lr.vo.AddFees;
import com.cetera.ng.workflow.lr.vo.AdditionalAgreementVO;
import com.cetera.ng.workflow.lr.vo.ArbitrationDisclosureVO;
import com.cetera.ng.workflow.lr.vo.BranchApplicationVO;
import com.cetera.ng.workflow.lr.vo.CommisionsDirectDepositVO;
import com.cetera.ng.workflow.lr.vo.EmploymentDetailVo;
import com.cetera.ng.workflow.lr.vo.EstimatedFeesVO;
import com.cetera.ng.workflow.lr.vo.EstimationFeeDetailsVo;
import com.cetera.ng.workflow.lr.vo.FingerPrintInstructionsVO;
import com.cetera.ng.workflow.lr.vo.IndependentContractAgreementVO;
import com.cetera.ng.workflow.lr.vo.InvestmentAdviserRepVo;
import com.cetera.ng.workflow.lr.vo.OSJHomeOfficeReviewVO;
import com.cetera.ng.workflow.lr.vo.OngoingFeeForm;
import com.cetera.ng.workflow.lr.vo.PersonalOrRelatedAccountDetailVo;
import com.cetera.ng.workflow.lr.vo.RegistrationKitVO;
import com.cetera.ng.workflow.lr.vo.RegistrationSubmissionReviewVO;
import com.cetera.ng.workflow.lr.vo.TaskDetailVO;
import com.cetera.ng.workflow.lr.vo.UnApprovedProductDetailVo;
import com.cetera.ng.workflow.lr.vo.UserAgreementVO;

@Controller
@RequestMapping(value = CeteraURIConstant.REGISTRATION_CONTROLLER_URI)
public class RegistrationController implements CeteraConstants {

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private UsersDetailService usersDetailService;
	
	@Autowired
	private FeesService feesService;

	private String moduleName = "registration";

	private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

	@RequestMapping(value = CeteraURIConstant.ADD_FEE_VIEW, method = RequestMethod.GET)
	public String addFeeDetailsView(ModelMap model, HttpSession session) {
		logger.info("entry");
		Users user = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			EstimationFeeDetailsVo estimateFeeDetailsView = feesService.getFeeDetails(user.getUsersDetail());
			model.addAttribute("EstimationFeeDetailsVo", estimateFeeDetailsView);
		} catch (CustomGenericException e) {
			logger.error("Error estimateFeeDetailsView ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("EstimationFeeDetailsVo", new EstimationFeeDetailsVo());
		}
		logger.info("exit");
		return "addFeeDetails";
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_EMPLOYEMENT_DETAILS, method = RequestMethod.GET)
	public String employmentDetailsView(ModelMap model, HttpSession session) {
		logger.info("entry");
		Users user = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			EmploymentDetailVo employmentDetails = registrationService.getEmploymentDetails(user);
			String pageName = "employmentDetails.jsp";
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			employmentDetails.setAgreementVO(userAgreementVO);

			UserRegistrationPageSubmit pageSubmit = registrationService
					.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			employmentDetails.setPageSubmit(pageSubmit);

			model.addAttribute("EmploymentDetails", employmentDetails);
		} catch (CustomGenericException e) {
			logger.error("Error employmentDetailsView ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("EmploymentDetails", new EmploymentDetailVo());
		}
		logger.info("exit");
		return "employmentDetails";
	}

	@RequestMapping(value = CeteraURIConstant.ADD_EMPLOYEMENT_DETAILS, method = RequestMethod.POST)
	public String saveEmploymentDetails(ModelMap model,
			@ModelAttribute("EmploymentDetails") EmploymentDetailVo employmentDetailVo, HttpSession session) {

		logger.info("entry");
		String newPageURL = null;
		Users user = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			registrationService.saveEmploymentDetails(user, employmentDetailVo);
			if (employmentDetailVo.getAgreementVO().getFlag() == 1
					&& employmentDetailVo.getAgreementVO().getIsActive()) {
				employmentDetailVo.getAgreementVO().setUserId(user);
				registrationService.submitAgreementDetails(employmentDetailVo.getAgreementVO());
			}
			registrationService.saveUserRegistrationPageSubmit(employmentDetailVo.getPageSubmit(), user);

			newPageURL = ServiceUtil.getPreviousOrNextURL(employmentDetailVo.getAgreementVO(), user);

		} catch (CustomGenericException e) {
			logger.error("Error saveEmploymentDetails ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_EMPLOYEMENT_DETAILS : newPageURL);

	}

	@RequestMapping(value = CeteraURIConstant.VIEW_FINGER_PRINT_INSTRUCTIONS, method = RequestMethod.GET)
	public String viewFingerPrintInstructionsPage(ModelMap model, HttpSession session) {
		Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
		try {
			logger.info("entry");
			String pageName = "fingerprintdetail.jsp";
			FingerPrintInstructionsVO fingerPrintInstructionsVO = new FingerPrintInstructionsVO();

			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			fingerPrintInstructionsVO.setUserAgreementVO(userAgreementVO);

			UserRegistrationPageSubmit pageSubmit = registrationService
					.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			fingerPrintInstructionsVO.setPageSubmit(pageSubmit);

			model.addAttribute("fingerPrintInstructionsVO", fingerPrintInstructionsVO);
		} catch (CustomGenericException e) {
			logger.error("Error viewFingerPrintInstructionsPage ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("fingerPrintInstructionsVO", new FingerPrintInstructionsVO());
		}
		logger.info("exit");
		return "fingerPrintInstructions";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_FINGERPRINT_AGREEMENT_DETAILS, method = RequestMethod.GET)
	public String submitAgreementDetails(
			@ModelAttribute("fingerPrintInstructionsVO") FingerPrintInstructionsVO fingerPrintInstructionsVO,
			HttpSession session, ModelMap model) {
		logger.info("entry");
		Users user = null;
		String newPageURL = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			if (fingerPrintInstructionsVO.getUserAgreementVO().getFlag() == 1
					&& fingerPrintInstructionsVO.getUserAgreementVO().getIsActive()) {
				fingerPrintInstructionsVO.getUserAgreementVO().setUserId(user);
			
				registrationService.submitAgreementDetails(fingerPrintInstructionsVO.getUserAgreementVO());
			}
			registrationService.saveUserRegistrationPageSubmit(fingerPrintInstructionsVO.getPageSubmit(), user);

			newPageURL = ServiceUtil.getPreviousOrNextURL(fingerPrintInstructionsVO.getUserAgreementVO(), user);
		} catch (CustomGenericException e) {
			logger.error("Error submitAgreementDetails ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_FINGER_PRINT_INSTRUCTIONS : newPageURL);
	}

	@RequestMapping(value = CeteraURIConstant.UN_APPROVED_PRODUCT, method = RequestMethod.GET)
	public String unApprovedProductView(ModelMap model, HttpSession session,
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestParam(value = "sourcePage", required = false) String sourcePage,
			@RequestParam(value = "taskId", required = false) Long taskId,
			@RequestParam(value = "processInstanceId", required = false) Long processInstanceId) {
		logger.info("entry");
		try {
			Users user = null;
			if (userId != null) {
				user = usersDetailService.getUserById(userId);
			} else {
				user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			}
			UnApprovedProductDetailVo userUnApprovedProductDetail = registrationService
					.getUserUnApprovedProductDetail(user);
			userUnApprovedProductDetail.setSourcePage(sourcePage);
			userUnApprovedProductDetail.setUserId(userId != null ? userId : 0);
			userUnApprovedProductDetail.setTaskId(taskId != null ? taskId : 0);
			userUnApprovedProductDetail.setProcessInstanceId(processInstanceId != null ? processInstanceId : 0);

			String pageName = "unApprovedProduct.jsp";
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			userUnApprovedProductDetail.setAgreementVO(userAgreementVO);
			UserRegistrationPageSubmit pageSubmit = registrationService
					.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			userUnApprovedProductDetail.setPageSubmit(pageSubmit);
			model.addAttribute("UnApprovedProductDetail", userUnApprovedProductDetail);
		} catch (CustomGenericException e) {
			logger.error("Error unApprovedProductView ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("UnApprovedProductDetail", new UnApprovedProductDetailVo());
		}
		logger.info("exit");
		return "unApprovedProduct";
	}

	@RequestMapping(value = CeteraURIConstant.UN_APPROVED_PRODUCT_SAVE, method = RequestMethod.POST)
	public String unApprovedProductSave(ModelMap model,
			@ModelAttribute("UnApprovedProductDetail") UnApprovedProductDetailVo unapprovedProductDetails,
			HttpSession session) {
		logger.info("entry");
		String newPageURL = null;
		try {
			if (CommonUtils.isEmpty(unapprovedProductDetails.getSourcePage())) {
				Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
				registrationService.saveUserUnApprovedProductDetail(user,
						unapprovedProductDetails.getUnapprovedProductDetails());
				if (unapprovedProductDetails.getAgreementVO().getFlag() == 1
						&& unapprovedProductDetails.getAgreementVO().getIsActive()) {
					unapprovedProductDetails.getAgreementVO().setUserId(user);
					registrationService.submitAgreementDetails(unapprovedProductDetails.getAgreementVO());
				}
				registrationService.saveUserRegistrationPageSubmit(unapprovedProductDetails.getPageSubmit(), user);
				newPageURL = ServiceUtil.getPreviousOrNextURL(unapprovedProductDetails.getAgreementVO(), user);
			} else {
				switch (unapprovedProductDetails.getSourcePage()) {
				case OSJ_REGISTRATION_REVIEW_PAGE:
					newPageURL = CeteraURIConstant.REGISTRATION_CONTROLLER_URI+"/osjHomeOfficeReview/" + unapprovedProductDetails.getTaskId() + "/"
							+ unapprovedProductDetails.getUserId() + "/" +unapprovedProductDetails.getProcessInstanceId();
					break;
				case REGISTRATION_REVIEW_FINAL_SUBMISSION_PAGE:
					newPageURL = CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_REGISTRATION_KIT_SUBMISSION_VIEW;
					break;
				}
			}
		} catch (CustomGenericException e) {
			logger.error("Error unApprovedProductSave ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.UN_APPROVED_PRODUCT : newPageURL);
	}

	@RequestMapping(value = CeteraURIConstant.ADD_FEE, method = RequestMethod.POST)
	public String addFee(@ModelAttribute("AddFees") AddFees addFees, HttpSession session) {
		logger.info("entry");
		Users user = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			feesService.updateUserFees(addFees, user.getUsersDetail());
		} catch (CustomGenericException e) {
			logger.error("Error addFee", e);
		}
		logger.info("exit");
		return "redirect:" + ("/registration/ongoingFeeView");
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_IAR_FORM, method = RequestMethod.GET)
	public String iARApplicationForm(ModelMap model, HttpSession session,
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestParam(value = "sourcePage", required = false) String sourcePage,
			@RequestParam(value = "taskId", required = false) Long taskId,
			@RequestParam(value = "processInstanceId", required = false) Long processInstanceId) {
		logger.info("entry");
		Users user = null;
		try {
			String pageName = "iarApplicationForm.jsp";
			if (userId != null) {
				user = usersDetailService.getUserById(userId);
			} else {
				user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			}
			UserAgreementVO userAgreementDetails = getUserAgreementDetails(user, pageName, moduleName);
			InvestmentAdviserRepVo investmentAdviserRepresentativeInfo = registrationService
					.getInvestmentAdviserRepresentativeInfo(user);
			investmentAdviserRepresentativeInfo.setSourcePage(sourcePage);
			investmentAdviserRepresentativeInfo.setUserId(userId != null ? userId : 0);
			investmentAdviserRepresentativeInfo.setTaskId(taskId != null ? taskId : 0);
			investmentAdviserRepresentativeInfo.setProcessInstanceId(processInstanceId != null ? processInstanceId : 0);
			investmentAdviserRepresentativeInfo.setAgreementVO(userAgreementDetails);
			UserRegistrationPageSubmit pageSubmit = registrationService
					.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			investmentAdviserRepresentativeInfo.setPageSubmit(pageSubmit);
			model.addAttribute("InvestmentAdviserRepresentative", investmentAdviserRepresentativeInfo);
		} catch (CustomGenericException e) {
			logger.error("Error iARApplicationForm ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("InvestmentAdviserRepresentative", new InvestmentAdviserRepVo());
		}
		logger.info("exit");
		return "iarForm";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_IAR_FORM, method = RequestMethod.POST)
	public String submitIARForm(
			@ModelAttribute("InvestmentAdviserRepresentative") InvestmentAdviserRepVo investmentAdviserRepVo,
			HttpSession session, ModelMap model) {
		logger.info("entry");
		String newPageURL = null;
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			if (CommonUtils.isEmpty(investmentAdviserRepVo.getSourcePage())) {
				registrationService.saveIARDetails(investmentAdviserRepVo, user);
				if (investmentAdviserRepVo.getAgreementVO().getFlag() == 1
						&& investmentAdviserRepVo.getAgreementVO().getIsActive()) {
					investmentAdviserRepVo.getAgreementVO().setUserId(user);
					registrationService.submitAgreementDetails(investmentAdviserRepVo.getAgreementVO());
				}
				newPageURL = ServiceUtil.getPreviousOrNextURL(investmentAdviserRepVo.getAgreementVO(), user);
			} else {
				switch (investmentAdviserRepVo.getSourcePage()) {
				case OSJ_REGISTRATION_REVIEW_PAGE:
					newPageURL = CeteraURIConstant.REGISTRATION_CONTROLLER_URI+"/osjHomeOfficeReview/" + investmentAdviserRepVo.getTaskId() + "/"
							+ investmentAdviserRepVo.getUserId() + "/" +investmentAdviserRepVo.getProcessInstanceId();
					break;
				case REGISTRATION_REVIEW_FINAL_SUBMISSION_PAGE:
					newPageURL = CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_REGISTRATION_KIT_SUBMISSION_VIEW;
					break;
				}
			}
		} catch (CustomGenericException e) {
			logger.error("Error submitIARForm ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_IAR_FORM : newPageURL);
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_ARBITRATION_DISCLOSURE, method = RequestMethod.GET)
	public String arbitrationDisclosureForm(HttpSession session, ModelMap model) {
		logger.info("entry");
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			String pageName = "arbitrationDisclosureForm.jsp";
			ArbitrationDisclosureVO arbitrationDisclosureVO = new ArbitrationDisclosureVO();
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			arbitrationDisclosureVO.setUserAgreementVO(userAgreementVO);

			UserRegistrationPageSubmit pageSubmit = registrationService
					.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			arbitrationDisclosureVO.setPageSubmit(pageSubmit);

			model.addAttribute("arbitrationDisclosureVO", arbitrationDisclosureVO);
		} catch (CustomGenericException e) {
			logger.error("Error arbitrationDisclosureForm ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("arbitrationDisclosureVO", new ArbitrationDisclosureVO());
		}
		logger.info("exit");
		return "arbitrationDisclosure";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_ARBITRATION_DISCLOSURE, method = RequestMethod.GET)
	public String submitArbitrationDisclosure(
			@ModelAttribute("arbitrationDisclosureVO") ArbitrationDisclosureVO arbitrationDisclosureVO,
			HttpSession session, ModelMap model) {
		logger.info("entry");
		Users user = null;
		String newPageURL = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			if (arbitrationDisclosureVO.getUserAgreementVO().getFlag() == 1
					&& arbitrationDisclosureVO.getUserAgreementVO().getIsActive()) {
				arbitrationDisclosureVO.getUserAgreementVO().setUserId(user);
				registrationService.submitAgreementDetails(arbitrationDisclosureVO.getUserAgreementVO());
			}
			registrationService.saveUserRegistrationPageSubmit(arbitrationDisclosureVO.getPageSubmit(), user);

			newPageURL = ServiceUtil.getPreviousOrNextURL(arbitrationDisclosureVO.getUserAgreementVO(), user);
		} catch (CustomGenericException e) {
			logger.error("Error submitArbitrationDisclosure ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_ARBITRATION_DISCLOSURE : newPageURL);
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_PRA_FORM, method = RequestMethod.GET)
	public String personalOrRelatedAccounts(ModelMap model, HttpSession session) {
		logger.info("entry");
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			String pageName = "personalOrRelatedAccountsForm.jsp";
			UserAgreementVO userAgreementDetails = getUserAgreementDetails(user, pageName, moduleName);
			PersonalOrRelatedAccountDetailVo personalOrRelatedAccountDetail = registrationService
					.getPersonalOrRelatedAccountDetail(user);
			personalOrRelatedAccountDetail.setUserAgreementVO(userAgreementDetails);
			model.addAttribute("PersonalOrRelatedAccountDetail", personalOrRelatedAccountDetail);
		} catch (CustomGenericException e) {
			logger.error("Error personalOrRelatedAccounts ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("PersonalOrRelatedAccountDetail", new PersonalOrRelatedAccountDetailVo());
		}
		logger.info("exit");
		return "personalOrRelatedAccounts";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_PRA_FORM, method = RequestMethod.POST)
	public String submitPRAForm(ModelMap model,
			@ModelAttribute("PersonalOrRelatedAccountDetail") PersonalOrRelatedAccountDetailVo personalOrRelatedAccountDetailVo,
			HttpSession session) {
		logger.info("entry");
		String newURL = "personalOrRelatedAccounts";
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			registrationService.savePersonalOrRelatedAccountDetail(personalOrRelatedAccountDetailVo, user);
			if (personalOrRelatedAccountDetailVo.getUserAgreementVO().getFlag() == 1
					&& personalOrRelatedAccountDetailVo.getUserAgreementVO().getIsActive()) {
				personalOrRelatedAccountDetailVo.getUserAgreementVO().setUserId(user);
				registrationService.submitAgreementDetails(personalOrRelatedAccountDetailVo.getUserAgreementVO());
			}
			newURL = ServiceUtil.getPreviousOrNextURL(personalOrRelatedAccountDetailVo.getUserAgreementVO(), user);
		} catch (CustomGenericException e) {
			logger.error("Error submitPRAForm ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_PRA_FORM : newURL);
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_IC_AGREEMENTS, method = RequestMethod.GET)
	public String independentAgreementFormView(HttpSession session, ModelMap model,
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestParam(value = "sourcePage", required = false) String sourcePage,
			@RequestParam(value = "taskId", required = false) Long taskId,
			@RequestParam(value = "processInstanceId", required = false) Long processInstanceId) {
		logger.info("entry");
		try {
			Users user = null;
			IndependentContractAgreementVO independentContractAgreementVO = new IndependentContractAgreementVO();
			if (userId != null) {
				user = usersDetailService.getUserById(userId);
			} else {
				user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			}
			String pageName = "ICAgreements.jsp";
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			independentContractAgreementVO.setUserAgreementVO(userAgreementVO);

			independentContractAgreementVO.setSourcePage(sourcePage);
			independentContractAgreementVO.setTaskId(taskId != null ? taskId : 0);
			independentContractAgreementVO.setUserId(userId != null ? userId : 0);
			independentContractAgreementVO.setProcessInstanceId(processInstanceId != null ? processInstanceId : 0);

			UserRegistrationPageSubmit pageSubmit = registrationService
					.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			independentContractAgreementVO.setPageSubmit(pageSubmit);

			model.addAttribute("independentContractAgreement", independentContractAgreementVO);
		} catch (CustomGenericException e) {
			logger.error("Error independentAgreementFormView ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("independentContractAgreement", new IndependentContractAgreementVO());
		}
		logger.info("exit");
		return "ICAgreements";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_IC_AGREEMENTS, method = RequestMethod.GET)
	public String submitAdditionalAgreementsForm(
			@ModelAttribute("independentContractAgreement") IndependentContractAgreementVO independentContractAgreementVO,
			HttpSession session, ModelMap model) {
		logger.info("entry");
		Users user = null;
		String newPageURL = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			if (CommonUtils.isEmpty(independentContractAgreementVO.getSourcePage())) {
				if (independentContractAgreementVO.getUserAgreementVO().getFlag() == 1
						&& independentContractAgreementVO.getUserAgreementVO().getIsActive()) {
					independentContractAgreementVO.getUserAgreementVO().setUserId(user);
					registrationService.submitAgreementDetails(independentContractAgreementVO.getUserAgreementVO());
				}
				registrationService.saveUserRegistrationPageSubmit(independentContractAgreementVO.getPageSubmit(),
						user);
				newPageURL = ServiceUtil.getPreviousOrNextURL(independentContractAgreementVO.getUserAgreementVO(),
						user);
			} else {
				switch (independentContractAgreementVO.getSourcePage()) {
				case OSJ_REGISTRATION_REVIEW_PAGE:
					newPageURL = CeteraURIConstant.REGISTRATION_CONTROLLER_URI+"/osjHomeOfficeReview/" + independentContractAgreementVO.getTaskId() + "/"
							+ independentContractAgreementVO.getUserId() + "/" + independentContractAgreementVO.getProcessInstanceId();
					break;
				case REGISTRATION_REVIEW_FINAL_SUBMISSION_PAGE:
					newPageURL = CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_REGISTRATION_KIT_SUBMISSION_VIEW;
					break;
				}
			}
		} catch (CustomGenericException e) {
			logger.error("Error submitAdditionalAgreementsForm ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newPageURL == null ? "ICAgreements" : newPageURL);
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_FID_DUAL_FORM, method = RequestMethod.GET)
	public String fidRepresentativeAddendumDualView(HttpSession session, ModelMap model,
			@RequestParam(value = "pageSubmitFrom", required = false)String pageSubmitFrom) {
		logger.info("entry");
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			String pageName = "fIDRepresentativeAddendum.jsp";
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			if (CommonUtils.isNotEmpty(pageSubmitFrom))
				userAgreementVO.setPageSubmitFrom(pageSubmitFrom);
			model.addAttribute("UserAgreementVO", userAgreementVO);
		} catch (CustomGenericException e) {
			logger.error("Error fidRepresentativeAddendumDualView ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("independentContractAgreement", new UserAgreementVO());
		}
		logger.info("exit");
		return "fIDRepresentativeAddendum";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_FID_DUAL_FORM, method = RequestMethod.POST)
	public String fidRepresentativeAddendumDualSubmit(@ModelAttribute("UserAgreementVO") UserAgreementVO agreementVO,
			HttpSession session, ModelMap model) {
		logger.info("entry");
		String newPageURL = null;
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			agreementVO.setUserId(user);
			if (agreementVO.getFlag() == 1 && agreementVO.getIsActive()) {
				agreementVO.setUserId(user);
				registrationService.submitAgreementDetails(agreementVO);
			}
			if (CommonUtils.isEmpty(agreementVO.getPageSubmitFrom()))
				newPageURL = ServiceUtil.getPreviousOrNextURL(agreementVO, user);
			else
				newPageURL = "registrationSubmissionView";
		} catch (CustomGenericException e) {
			logger.error("Error fidRepresentativeAddendumDualSubmit ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_FID_DUAL_FORM : newPageURL);
	}

	private UserAgreementVO getUserAgreementDetails(Users user, String pageName, String moduleName)
			throws CustomGenericException {
		Long flag = ServiceUtil.getConsentValue(user, pageName, moduleName);
		UserAgreementVO userAgreementVO = null;
		if (flag == 1) {
			userAgreementVO = registrationService.getAgreementDetail(user.getUsersDetail().getId(), pageName,
					moduleName);
		}
		if (userAgreementVO == null) {
			userAgreementVO = new UserAgreementVO();
			userAgreementVO.setFlag(flag);
		} else {
			userAgreementVO.setFlag(flag);
		}
		return userAgreementVO;
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_ADDITIONAL_AGREEMENTS, method = RequestMethod.GET)
	public String viewAdditionalAgreements(HttpSession session, ModelMap model) {
		logger.info("entry");
		try {
			AdditionalAgreementVO additionalAgreementVO = new AdditionalAgreementVO();
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			String pageName = "additionalAgreements.jsp";
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			additionalAgreementVO.setUserAgreementVO(userAgreementVO);

			UserRegistrationPageSubmit pageSubmit = registrationService
					.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			additionalAgreementVO.setPageSubmit(pageSubmit);

			model.addAttribute("additionalAgreement", additionalAgreementVO);
		} catch (CustomGenericException e) {
			logger.error("Error additionalAgreementsForm ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("additionalAgreement", new AdditionalAgreementVO());
		}
		logger.info("exit");
		return "additionalAgreements";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_ADDITIONAL_AGREEMENTS, method = RequestMethod.GET)
	public String submitAdditionalAgreements(
			@ModelAttribute("additionalAgreement") AdditionalAgreementVO additionalAgreementVO, HttpSession session,
			ModelMap model) {
		logger.info("entry");
		String newPageURL = null;
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			additionalAgreementVO.getUserAgreementVO().setUserId(user);
			if (additionalAgreementVO.getUserAgreementVO().getFlag() == 1
					&& additionalAgreementVO.getUserAgreementVO().getIsActive()) {
				additionalAgreementVO.getUserAgreementVO().setUserId(user);
				registrationService.submitAgreementDetails(additionalAgreementVO.getUserAgreementVO());
			}
			registrationService.saveUserRegistrationPageSubmit(additionalAgreementVO.getPageSubmit(), user);
			newPageURL = ServiceUtil.getPreviousOrNextURL(additionalAgreementVO.getUserAgreementVO(), user);
		} catch (CustomGenericException e) {
			logger.error("Error Additional Agreement ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_ADDITIONAL_AGREEMENTS : newPageURL);
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_REG_KIT, method = RequestMethod.GET)
	public String submitRegKit(@ModelAttribute("mod") String module, Model model, HttpSession session) {

		logger.info("entry");
		String moduleName = null;
		List<PageModuleBDGMapping> pageModuleBDGMappingList = null;
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			if ("reg".equals(module))
				moduleName = "registration";
			pageModuleBDGMappingList = registrationService.getPageModMappingByModGroupBrokerStatus(user, moduleName);
			if (pageModuleBDGMappingList != null && pageModuleBDGMappingList.size() > 0) {
				session.setAttribute("pageModuleBDGMappingList", pageModuleBDGMappingList);
			}
		} catch (CustomGenericException e) {
			logger.error("Exception while retriving data from page module mapping", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "submitRegKit";
	}

	/**
	 * 
	 * @param session
	 * @param model
	 * @return registrationKit view
	 */

	@RequestMapping(value = CeteraURIConstant.VIEW_REGISTRATION_KIT, method = RequestMethod.GET)
	public String registrationKit(HttpSession session, Model model) {

		logger.info("entry");
		try {
			String pageName = "registrationKit.jsp";
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			RegistrationKitVO registrationKitVO = registrationService.getRegistrationKitDetail(user.getUsersDetail().getId(),
					user.getUsersDetail().getBrokerDealer());
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			registrationKitVO.setUserAgreementVO(userAgreementVO);
			UserRegistrationPageSubmit pageSubmit = registrationService.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			registrationKitVO.setPageSubmit(pageSubmit);
			model.addAttribute("registrationKitVO", registrationKitVO);
		} catch (CustomGenericException e) {
			logger.error("Error registrationKit ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("registrationKitVO", new RegistrationKitVO());
		}
		logger.info("exit");
		return "registrationKit";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_REGISTRATION_KIT, method = RequestMethod.POST)
	public String submitRegistrationKit(@ModelAttribute("registrationKitVO") RegistrationKitVO registrationKitVO,
			HttpSession session, ModelMap model) {
		logger.info("entry");
		Users user = null;
		String newPageURL = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			registrationService.saveRegistrationKitDetail(registrationKitVO, user);
			if (registrationKitVO.getUserAgreementVO().getFlag() == 1
					&& registrationKitVO.getUserAgreementVO().getIsActive()) {
				registrationKitVO.getUserAgreementVO().setUserId(user);
				registrationService.submitAgreementDetails(registrationKitVO.getUserAgreementVO());
			}
			newPageURL = ServiceUtil.getPreviousOrNextURL(registrationKitVO.getUserAgreementVO(), user);
		} catch (CustomGenericException e) {
			logger.error("Error submitRegistrationKit",e.getMessage());
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		logger.info("exit");
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_REGISTRATION_KIT : newPageURL);
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_BRANCH_APPLICATION, method = RequestMethod.GET)
	public String viewBranchApplication(HttpSession session, ModelMap model) {
		logger.info("entry");
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			BranchApplicationVO branchApplicationVO = registrationService.getBranchApplcationDetail(user);
			model.addAttribute("branchApplicationVO", branchApplicationVO);
		} catch (CustomGenericException e) {
			logger.error("Error registrationKit ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("branchApplicationVO", new BranchApplicationVO());
		}
		logger.info("exit");
		return "branchApplication";
	}

	@RequestMapping(value = CeteraURIConstant.SUBMIT_BRANCH_APPLICATION, method = RequestMethod.POST)
	public String submitBranchApplication(
			@ModelAttribute("branchApplicationVO") BranchApplicationVO branchApplicationVO, HttpSession session,
			ModelMap model) {
		logger.info("entry");
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			registrationService.submitBranchApplication(branchApplicationVO, user);
		} catch (CustomGenericException e) {
			logger.error("Error submitBranchApplication ", e);
		}
		logger.info("exit");
		return "branchApplication";
	}

	@RequestMapping(value = CeteraURIConstant.VIEW_REGISTRATION_KIT_SUBMISSION_VIEW, method = RequestMethod.GET)
	public String registrationKitFinalSubmissionView(HttpSession session, Model model,@RequestParam(value = "submissionMassage", required = false)String submissionMassage) {

		logger.info("entry");
		try {
			String pageName = "registrationFinalReviewAndSubmisstion.jsp";
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			RegistrationSubmissionReviewVO registrationSubmissionReviewVO = registrationService
					.getRegistrationSubmissionReview(user.getUsersDetail().getId(), user);
			registrationSubmissionReviewVO.setSubmissionMassage(submissionMassage);
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			registrationSubmissionReviewVO.setUserAgreementVO(userAgreementVO);
			UserRegistrationPageSubmit pageSubmit = registrationService.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			registrationSubmissionReviewVO.setPageSubmit(pageSubmit);
			model.addAttribute("RegistrationSubmissionReviewVO", registrationSubmissionReviewVO);
		} catch (CustomGenericException e) {
			logger.error("Error registrationKit ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("RegistrationSubmissionReviewVO", new RegistrationSubmissionReviewVO());
		}
		logger.info("exit");
		return "registrationSubmissionReview";
	}

	/**
	 * 
	 * @param model
	 * @return commisionDirectDeposit view
	 */
	@RequestMapping(value = CeteraURIConstant.VIEW_COMMISION_DIRECT_DEPOSIT, method = RequestMethod.GET)
	public String viewCommisionDirectDeposit(Model model, HttpSession session) {
		logger.info("exit");
		try {
			String pageName = "commisionDirectDeposit.jsp";
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			CommisionsDirectDepositVO commisionsDirectDepositVO = registrationService.getCommisionsDirectDeposit(user);
			UserAgreementVO userAgreementVO = getUserAgreementDetails(user, pageName, moduleName);
			commisionsDirectDepositVO.setUserAgreementVO(userAgreementVO);
			UserRegistrationPageSubmit pageSubmit = registrationService.getUserRegistrationPageSubmit(user.getUsersDetail().getId(), pageName);
			commisionsDirectDepositVO.setPageSubmit(pageSubmit);
			model.addAttribute("commisionsDirectDepositVO", commisionsDirectDepositVO);
		} catch (CustomGenericException e) {
			logger.error("Error viewCommisionDirectDeposit ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("commisionsDirectDepositVO", new CommisionsDirectDepositVO());
		}
		return "commisionDirectDeposit";
	}

	/**
	 * 
	 * @param commisionsDirectDepositVO
	 * @param model
	 * @return commisionDirectDeposit view
	 */

	@RequestMapping(value = CeteraURIConstant.SUBMIT_COMMISION_DIRECT_DEPOSIT, method = RequestMethod.POST)
	public String submitCommisionDirectDeposit(
			@ModelAttribute("commisionsDirectDepositVO") CommisionsDirectDepositVO commisionsDirectDepositVO,
			Model model, HttpSession session) {
		String newPageURL = null;
		try {
			Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			registrationService.saveCommisionsDirectDeposit(commisionsDirectDepositVO, user);
			newPageURL = ServiceUtil.getPreviousOrNextURL(commisionsDirectDepositVO.getUserAgreementVO(), user);
		} catch (CustomGenericException e) {
			logger.error("Error ongoingFeeDetailsView ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
		}
		return "redirect:" + (newPageURL == null ? CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_COMMISION_DIRECT_DEPOSIT : newPageURL);
	}

	@RequestMapping(value = CeteraURIConstant.ESTIMATED_FEE_VIEW, method = RequestMethod.GET)
	public String estimateFeeDetailsView(ModelMap model, HttpSession session) {
		logger.info("entry");
		Users user = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			EstimatedFeesVO estimatedFeesVO = feesService.estimatedFeeDetails(user.getUsersDetail());
			model.addAttribute("estimatedFeesVO", estimatedFeesVO);
		} catch (CustomGenericException e) {
			logger.error("Error ongoingFeeDetailsView ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("estimatedFeesVO", new EstimatedFeesVO());
		}
		logger.info("exit");
		return "estimatedFeeDetails";
	}

	@RequestMapping(value = CeteraURIConstant.ONGOING_FEE_VIEW, method = RequestMethod.GET)
	public String ongoingFeeDetailsView(ModelMap model, HttpSession session) {
		logger.info("entry");
		Users user = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			OngoingFeeForm ongoingFeeForm = feesService.onGoingFeeDetails(user.getUsersDetail());
			ongoingFeeForm.setBrokerDealerName(user.getUsersDetail().getBrokerDealer().getBrokerDealerName());
			if(null == ongoingFeeForm.getYear()){
				ongoingFeeForm.setYear(ServiceUtil.getCuurentYear());
			}
			model.addAttribute("ongoingFeeForm", ongoingFeeForm);
		} catch (CustomGenericException e) {
			logger.error("Error ongoingFeeDetailsView ", e);
			model.addAttribute("errorCode", e.getErrorCode());
			model.addAttribute("errorDescription", ErrorProperties.getInstance().getProperty(e.getErrorCode()));
			model.addAttribute("ongoingFeeForm", new OngoingFeeForm());
		}
		logger.info("exit");
		return "ongoingFeeView";
	}

	@RequestMapping(value = CeteraURIConstant.ADD_ONGOING_FEE, method = RequestMethod.POST)
	public String addOngoingFee(@ModelAttribute("OngoingFeeForm") OngoingFeeForm ongoingFeeForm, HttpSession session) {
		Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
		try {
			feesService.updateOngoingFees(ongoingFeeForm, user);
		} catch (CustomGenericException e) {
			logger.error("Error ongoingFeeDetailsView ", e);
		}
		return "redirect:" + ("/registration/estimatedFeeView");
	}

	@RequestMapping(value = CeteraURIConstant.REGISTRATION_KIT_SUBMIT, method = RequestMethod.POST)
	public String registrationKitFinalSubmission(HttpSession session, Model model) {
		logger.info("entry");
		Users user = null;
		String registrationKitSubmit = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			registrationKitSubmit = registrationService.registrationKitSubmit(user);
		} catch (Exception e) {
			logger.error("Error registrationKitFinalSubmission", e);
		}
		logger.info("exit");
		return "redirect:" + CeteraURIConstant.REGISTRATION_CONTROLLER_URI+CeteraURIConstant.VIEW_REGISTRATION_KIT_SUBMISSION_VIEW+"?submissionMassage="+registrationKitSubmit;
	}

	@RequestMapping(value = CeteraURIConstant.ASSIGNED_TASK, method = RequestMethod.GET)
	public String getAssignedTask(HttpSession session, Model model) {
		logger.info("entry");
		Users user = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			List<TaskDetailVO> taskDetailList = registrationService.getAssignedTask(user);
			model.addAttribute("taskDetailList", taskDetailList);
		} catch (Exception e) {
			logger.error("Error registrationKitFinalSubmission", e);
			model.addAttribute("taskDetailList", new ArrayList<TaskDetailVO>());
		}
		logger.info("exit");
		return "assignedTask";
	}

	@RequestMapping(value = CeteraURIConstant.OSJ_HOME_REVIEW, method = RequestMethod.GET)
	public String osjHomeOfficeReview(HttpSession session, Model model, @PathVariable("taskId") long taskId,
			@PathVariable("userId") long userId,@PathVariable("processInstanceId") long processInstanceId) {
		logger.info("entry");
		OSJHomeOfficeReviewVO osjHomeOfficeReviewVO = new OSJHomeOfficeReviewVO();
		osjHomeOfficeReviewVO.setTaskId(taskId);
		osjHomeOfficeReviewVO.setUserId(userId);
		osjHomeOfficeReviewVO.setProcessInstanceId(processInstanceId);
		model.addAttribute("OSJHomeOfficeReviewVO", osjHomeOfficeReviewVO);
		logger.info("exit");
		return "OSJHomeOfficeReview";
	}

	@RequestMapping(value = CeteraURIConstant.OSJ_HOME_REVIEW_SUBMIT, method = RequestMethod.POST)
	public String osjHomeOfficeReviewSubmited(HttpSession session, Model model,
			@ModelAttribute("OSJHomeOfficeReviewVO") OSJHomeOfficeReviewVO osjHomeOfficeReviewVO) {
		logger.info("entry");
		Users user = null;
		try {
			user = (Users) session.getAttribute(LOGIN_USER_SESSION);
			registrationService.osjReviewSubmit(user, osjHomeOfficeReviewVO);
		} catch (Exception e) {
			logger.error("Error registrationKitFinalSubmission", e);
		}
		logger.info("exit");
		return "OSJHomeOfficeReview";
	}
	
	@RequestMapping(value = CeteraURIConstant.ADD_COMMISION_DETAIL_VIEW, method = RequestMethod.GET)
	public String AddCommissionsDetailsView(HttpSession session, Model model) {
		logger.info("entry");
		
		logger.info("exit");
		return "addCommissionsDetails";
	}
	
	@RequestMapping(value = CeteraURIConstant.UPDATE_ESTIMATE_FEE, method = RequestMethod.POST)
	public String addEstimateFee(@ModelAttribute("EstimatedFeesVO") EstimatedFeesVO estimatedFeesVO, HttpSession session) {
		Users user = (Users) session.getAttribute(LOGIN_USER_SESSION);
		try {
			feesService.updateEstimatedFees(estimatedFeesVO, user);
		} catch (CustomGenericException e) {
			logger.error("Error registrationKitFinalSubmission", e);
		}
		return "redirect:" + ("/registration/estimatedFeeView");
	}

}
