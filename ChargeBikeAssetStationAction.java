package cn.yunrui.chargebike.asset.action;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.cc.cisp.code.entity.Code;
import cn.cc.cisp.code.util.CodeUtil;
import cn.cc.cisp.logging.CispLogger;
import cn.cc.cisp.model.dto.ChangeValue;
import cn.cc.cisp.model.dto.IModelEntity;
import cn.cc.cisp.model.util.BasePersistUtil;
import cn.cc.cisp.security.web.context.RequestContextSecurity;
import cn.yunrui.chargebike.asset.service.ChargeStationMtcService;
import cn.yunrui.chargebike.asset.service.ChargeflszService;
import cn.yunrui.chargebike.asset.util.ChargeBikeConstant;
import cn.yunrui.common.util.JsonDateValueProcessor;
import cn.yunrui.common.util.UUID;
import cn.yunrui.common.util.YunRuiCommonUtil;
import cn.yunrui.common.util.YunRuiDateUtil;
import cn.yunrui.common.util.YunRuiExcelUtil;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

@Controller
@RequestMapping("/chargeBikeAssetStation")
public class ChargeBikeAssetStationAction {

	@Resource
	private ChargeStationMtcService chargeStationMtcService;
    
	@Resource
	private ChargeflszService chargeflszService;
	
	private static final CispLogger logger = CispLogger.getLogger(ChargeBikeAssetStationAction.class);


	@RequestMapping("/Init.htm")
	public String init(Model model) {
		logger.debug("chargeBikeAsset/chargeBikeStationMan");
		return "chargeBikeAsset/chargeBikeStationMan";
	}
	@RequestMapping("/fwfInit.htm")
	public String fwfInit(Model model) {
		return "chargeBikeAsset/chargeStationfwf";
	}
	@RequestMapping(value="/getOrgList.htm",produces="text/plain;charset=UTF-8")
	public @ResponseBody String getOrgList(){
		List<Map<String,String>> StatusList = chargeStationMtcService.getOrgList();
		List<Map<String,String>> optionList=new ArrayList<Map<String,String>>();
		List<Map<String,String>> namelist=new ArrayList<Map<String,String>>();
		Map<String,String> map;
		for(int i=0;i<StatusList.size();i++){
			map=new HashMap<String,String>();
			map.put("id",StatusList.get(i).get("subburo"));
			map.put("name", StatusList.get(i).get("orgoprName"));
			optionList.add(map);
		}
		if(StatusList.size()!=1)
		{
			map=new HashMap<String,String>();
			map.put("id", "");
			map.put("name", "全部");			
			namelist.add(map);
		}
		namelist.addAll(optionList);
		JsonConfig json=new JsonConfig();
		System.out.println(">>>>>>>>>>optionList<<<<<<<<<<<"+optionList);
		return JSONArray.fromObject(namelist, json).toString();
		
	}
	//电站状态查询
	@RequestMapping(value = "/getChargeStationStatusList.htm", produces = "text/plain;charset=UTF-8")
	public @ResponseBody String getChargeCdzlxList() {
		StringBuilder sb = new StringBuilder();
		sb.append("[{'id':'','name':'全部'}");
			sb.append(",{'id':'").append("10").append("','name':'")
					.append("未投运").append("'}");
			sb.append(",{'id':'").append("20").append("','name':'")
			.append("在运").append("'}");
			sb.append(",{'id':'").append("40").append("','name':'")
			.append("退运").append("'}");
		
		sb.append("]");
		return sb.toString();
		
	}
	@RequestMapping(value = "/queryChargeBikeStationInMeter.htm", produces = "text/plain;charset=UTF-8")
	public @ResponseBody String queryChargeStationInMeter(HttpServletRequest request) throws Exception {
		try {
			String subburo_no = RequestContextSecurity.getAuthentication().getOrgNo();
//			String subburo_no ="88000242";
			String station =URLDecoder.decode(request.getParameter("station"), "UTF-8");
			System.out.println(">>>>>>>station<<<<<<<<<<<"+station);
			String stationNo = request.getParameter("stationNo");
			String status = request.getParameter("status");
			String schemeName = URLDecoder.decode(request.getParameter("schemeName"), "UTF-8");
			System.out.println(">>>>>>>schemeName<<<<<<<<<<<"+schemeName);
			String schemeType = request.getParameter("schemeType");
			String isReturn = request.getParameter("isReturn");
			int start = Integer.valueOf(request.getParameter("start"));
			int limit = Integer.valueOf(request.getParameter("limit"));
			Map<String, String> map = new HashMap<String, String>();
			map.put("station", station);
			map.put("stationNo", stationNo);
			map.put("status", status);
			map.put("schemeName", schemeName);
			map.put("schemeType", schemeType);
			map.put("isReturn", isReturn);
			List<Map<String, Object>> list = chargeStationMtcService.queryStation(subburo_no, map, start, limit);
			int total = chargeStationMtcService.getChargeStationCount(subburo_no, map);
			StringBuilder sb = new StringBuilder();
			sb.append("{total:" + total + ",results:[");
			for (int i = 0; i < list.size(); i++) {
				sb.append("{'id':'").append(list.get(i).get("id"))
				.append("','num':'").append(i+1)
				.append("','stationNo':'").append(YunRuiCommonUtil.nullToEmpty((String) list.get(i).get("stationNo")))							
				.append("','NAME':'").append(YunRuiCommonUtil.nullToEmpty((String) list.get(i).get("NAME")))
				.append("','chargedeviceNo':'").append(YunRuiCommonUtil.nullToEmpty((String) list.get(i).get("chargedeviceNo")))
				.append("','STATUS':'").append(!"".equals(YunRuiCommonUtil.nullToEmpty((String) list.get(i).get("STATUS")))?CodeUtil.getCodeName(ChargeBikeConstant.CODETYPE_Asset_YXZT,(String) list.get(i).get("STATUS")):"")
				.append("','plugCount':'").append(YunRuiCommonUtil.nullToEmpty(list.get(i).get("plugCount").toString()))
				.append("','schemeName':'").append(YunRuiCommonUtil.nullToEmpty((String) list.get(i).get("schemeName")))
				.append("','schemeType':'").append(!"".equals(YunRuiCommonUtil.nullToEmpty((String) list.get(i).get("schemeType")))?CodeUtil.getCodeName(ChargeBikeConstant.CODETYPE_CHARGE_FLJFFA,(String) list.get(i).get("schemeType")):"")
				.append("','isReturn':'").append(!"".equals(YunRuiCommonUtil.nullToEmpty((String) list.get(i).get("isReturn")))?CodeUtil.getCodeName(ChargeBikeConstant.CODETYPE_CHARGE_SFTF,(String) list.get(i).get("isReturn")):"")
				.append("'},");
			}
			sb.append("]}");
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "{total:0,results:[]}";
		}
	}
     
	@RequestMapping("/showStationDetail.htm")
	public String showStationDetail(Model model, HttpServletRequest request) {
		String flag = request.getParameter("flag");
		model.addAttribute("flag", flag);
		return "chargeBikeAsset/chargeBikeStationDetail";
	}
	
	@RequestMapping(value = "/initStationDetail.htm", produces = "text/plain;charset=UTF-8")
	public @ResponseBody String initStationDetail(String id) {
		logger.debug(">>>>>>>>>>>>>>>Station.initStationDetail.htm<<<<<<<<<<<<<<"+id);
		HashMap<String, Object> entityMap = null;
		logger.debug(">>>>>>>>>>>>>>>Station.initStationDetail.htm<<<<<<<<<<<<<<"+id);
		try {
			if(!"".equals(YunRuiCommonUtil.nullToEmpty(id))){
				entityMap = BasePersistUtil.getInstance()
						.getEntityValueMap("EBikeChargeStation", Long.parseLong(id));
				Map<String,String> map=chargeStationMtcService.getSchemeNameById(id);
				entityMap.put("schemeName", map.get("schemeName"));
				entityMap.put("schemeId", map.get("schemeId"));
				logger.debug(">>>>>>>>>>>>>>>Station.initStationDetail.htm<<<<<<<<<<<<<<"+entityMap.toString());
			List<Map<String,String>> list=addressResolution(entityMap.get("stationAddr").toString());
			for(int i=0;i<list.size();i++)
			{
				
				entityMap.put("province", list.get(i).get("province"));
				System.out.println(list.get(i).get("province"));
				entityMap.put("city", list.get(i).get("city"));
				entityMap.put("county", list.get(i).get("county"));
				entityMap.put("village", list.get(i).get("village"));
			}
			}
							  
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
			return JSONArray.fromObject(entityMap, jsonConfig).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
    
	@RequestMapping(value = "/saveStation.htm", produces = "text/plain;charset=UTF-8")
	public @ResponseBody String saveStation(HttpServletRequest request) throws Exception {		
		 
		   
        
		HashMap<String, Object> oldMap = new HashMap<String, Object>();
		HashMap<String, ChangeValue> cvMap = new HashMap<String, ChangeValue>();
		boolean result = false;
		String subburo_no = RequestContextSecurity.getAuthentication().getOrgNo();
		String creatorid=RequestContextSecurity.getAuthentication().getUserName();
		String orgName=RequestContextSecurity.getAuthentication().getName();
		System.out.println(">>>>>>>>>>>creatorid<<<<<<<<<<<<<<<<"+creatorid);
		String id = request.getParameter("id");
		boolean fg=false;
		try {
			String  schemeId=request.getParameter("schemeId");
			String name = request.getParameter("name");
			String stationNo=request.getParameter("stationNo");
			String citycode=request.getParameter("citycode");
			String adcode=request.getParameter("adcode");
			String jingdu = request.getParameter("jingdu");
			String weidu = request.getParameter("weidu");
			String Status=request.getParameter("Status");
			String stationAddr=request.getParameter("region");
			HashMap<String, Object> valueMap = new HashMap<String,Object>();	
			valueMap.put("name", name);
			valueMap.put("stationNo", stationNo);
			valueMap.put("citycode", citycode);
			valueMap.put("adcode",adcode);
			valueMap.put("longitude", jingdu);
			valueMap.put("latitude", weidu);
			valueMap.put("status",Status);		
			valueMap.put("CLASSNAME", "EBikeChargeStation");
			valueMap.put("subBuro", subburo_no);
			valueMap.put("buro", subburo_no);
			valueMap.put("stationAddr", stationAddr);						
			if (id == null || "".equals(id)) {
				valueMap.put("creatorID", creatorid);
				valueMap.put("createDate", new Date());
				IModelEntity entity=BasePersistUtil.getInstance().getNewModelEntity("EBikeChargeStation");
				entity.setEntityValue(valueMap);
				BasePersistUtil.getInstance().createModelEntity(entity);
				Long entityId=BasePersistUtil.getInstance().getNewEntityId();
				int  enid=entityId.intValue();
				String stationId=String.valueOf(enid-1);
				//计费方案 电站关系老表
				result=chargeflszService.addflsz(schemeId, stationId);	
				result=chargeflszService.addCost(new UUID().toString(),stationId,orgName);
				//添加计费方案电站关系表,使新表有一个默认值
				result=chargeflszService.addCsbsInfo(new UUID().toString(),schemeId,stationId,orgName);
				result = true;
			} else {
				valueMap.put("ID", id);
				valueMap.put("lastUpdateID", creatorid);
				valueMap.put("lastUpdateDate", new Date());
				System.out.println(valueMap.toString());
				oldMap = BasePersistUtil.getInstance().getEntityValueMap("EBikeChargeStation",
						Long.parseLong(id));
				oldMap.putAll(valueMap);
				cvMap = BasePersistUtil.getInstance().modifyModelEntity("EBikeChargeStation",
						oldMap);
				if(schemeId!=null&&"".equals(schemeId)&&schemeId.length()>0)//编辑时改动计费模板 才触发
				{
					fg=chargeflszService.deleteflById(id);
					result=chargeflszService.addflsz(schemeId, id);	
				}				
				result = true;
			}
//			logEntity.setType("正常");
			return "{'result':" + result + "}";
		} catch (Exception e) {
//			logEntity.setType("异常");
//			logEntity.setLogContent("\n" + e.toString());
			e.printStackTrace();
			return "{'result':" + result + "}";
		} finally {
			try {
//				logEntity.setType(result ? "正常" : "异常");
//				Authentication auth = RequestContextSecurity.getAuthentication();
//				logEntity.setIp(ChargeOrderUtil.getIpAddr(request));
//				logEntity.setTaskUuid(id);
//				logEntity.setOrgNo(auth.getOrgNo());
//				logEntity.setOrgName(auth.getOrgName());
//				logEntity.setDeptNo(auth.getDeptNo());
//				logEntity.setDeptName(auth.getDeptName());
//				logEntity.setTbrId(auth.getSysUserName());
//				logEntity.setTbrName(auth.getUserName());
//				logEntity.setLogContent("old-ChargeStation=" + oldMap.toString() + "\n,new-ChargeStation="
//						+ map.toString() + "\n,charge-ChargeStation=" + JSONArray.fromObject(cvMap).toString()
//						+ YunRuiCommonUtil.nullToEmpty(logEntity.getLogContent()));
//				AddLogThread thread = new AddLogThread(chargeCommonService, logEntity);
//				thread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@RequestMapping(value="/deleteStation.htm",produces="text/plain;charset=UTF-8")
	public @ResponseBody String deleteChargeSim(String ids){
		boolean result=true;
		String [] aa=ids.split(",");
		result=chargeStationMtcService.delete_rela(ids);	
		for(String id:aa ){
		  BasePersistUtil.getInstance().removeModelEntity("EBikeChargeStation", Long.parseLong(id));		 
		}
		return "{\"result\":"+result+"}";
	}
	
	@RequestMapping(value = "/exportChargeStationInMeter.htm")
	public void exportChargeStationInMeter(String station, String stationNo,
			HttpServletResponse response) throws Exception {
		try {
			String subburo_no = RequestContextSecurity.getAuthentication().getOrgNo();
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", station);
			map.put("stationNo", stationNo);
			List<Map<String, Object>> loadList = chargeStationMtcService.queryStation(subburo_no, map, -1, -1);
			List<List<String>> result = new ArrayList<List<String>>();
			if (loadList != null && loadList.size() > 0) {
				List<String> rowList = null;
				for (int i = 0; i < loadList.size(); i++) {
					rowList = new ArrayList<String>();
					rowList.add(String.valueOf(i + 1));
					rowList.add(YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("stationNo")));
					rowList.add(YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("region")));
					rowList.add(YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("NAME")));
					rowList.add(YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("chargedeviceNo")));
					rowList.add(YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("longitude")));
					rowList.add(YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("latitude")));					
					result.add(rowList);
				}
			}
			List<String> header = new ArrayList<String>();
			header.add("电站编号");
			header.add("地区");
			header.add("电站名称");
			header.add("设备编号");
			header.add("经度");
			header.add("纬度");
			YunRuiExcelUtil.toExcel("充电站维护", header, result, response, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@RequestMapping(value = "/checkStationNo.htm", produces = "text/plain;charset=UTF-8")
	public  @ResponseBody String checkStationNo(HttpServletRequest request)
	{
		String subburo_no = RequestContextSecurity.getAuthentication().getOrgNo();
//		String subburo_no ="88000242";
		boolean result=false;
		String stationNo=request.getParameter("stationNo");
		try{
		result=chargeStationMtcService.checkStationNoExist(stationNo,subburo_no);
		return "{result:"+result+"}";
		}catch(Exception ex){
			ex.printStackTrace();
			return "{'result':"+result+"}";						
		}
						
	}
	 public  List<Map<String,String>> addressResolution(String address){
	        String regex="(?<province>[^省]+省|.+自治区)(?<city>[^市]+市|.+自治州)(?<county>[^县]+县|.+区|.+市)?(?<village>.*)";
	        Matcher m=java.util.regex.Pattern.compile(regex).matcher(address);
	        String province=null,city=null,county=null,village=null;
	        List<Map<String,String>> table=new ArrayList<Map<String,String>>();
	        Map<String,String> row=null;
	        while(m.find()){
	            row=new LinkedHashMap<String,String>();
	            province=m.group("province");
	            row.put("province", province==null?"":province.trim());
	            city=m.group("city");
	            row.put("city", city==null?"":city.trim());
	            county=m.group("county");
	            row.put("county", county==null?"":county.trim());
	            village=m.group("village");
	            row.put("village", village==null?"":village.trim());
	            table.add(row);
	        }
	        return table;
	    }
	 
	 @RequestMapping(value="/checkResult.htm",produces="text/plain;charSet=UTF-8")
	 public @ResponseBody String checkResult(HttpServletRequest reqeust,String id)
	 {
		 boolean result=false;
		 result=chargeStationMtcService.checkResult(id);
		 return "{\"result\":"+result+"}";
	 }
	 //电站服务费率列表展示
	 @RequestMapping(value="/stationFwf.htm",produces="text/plain;charSet=UTF-8")
	 public @ResponseBody String stationFwf(HttpServletRequest request) throws UnsupportedEncodingException
	 {
		 String orgNo=request.getParameter("orgNo");
		 String stationNo=request.getParameter("stationNo");
		 String stationName=URLDecoder.decode(request.getParameter("stationName"), "utf-8");
		 String stationStatus=request.getParameter("stationStatus");
		 String devNo=request.getParameter("devNo");
		 Map<String,String> map=new HashMap<String,String>();
		 map.put("orgNo", orgNo);
		 map.put("stationNo", stationNo);
		 map.put("stationName", stationName);
		 map.put("stationStatus", stationStatus);
		 map.put("devNo", devNo);
		 Map<String,Object> resultMap=new HashMap<String,Object>();
		 List<Map<String,Object>> resultlist=new ArrayList<Map<String,Object>>(); 
		 resultMap.put("result", "");
		 resultMap.put("total", 0);    		
		 List<Map<String, Object>> loadList = chargeStationMtcService.getStationFwfData(map);
		 System.out.println(">>>>>>>>loadList<<<<<<<<<<<<"+loadList.toString());
		 int total = chargeStationMtcService.getStationFwfCount(map);
		 resultMap.put("total", total);
		 for (int i = 0; i < loadList.size(); i++) {	
			Map<String,Object> maps=new HashMap<String,Object>();
            maps.put("ID", loadList.get(i).get("chargestationId").toString());
            maps.put("chargestationNo", YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("chargestationNo")));
            maps.put("chargestationName", YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("chargestationName")));
            maps.put("orgName", YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("orgName")));
			maps.put("STATUS", !"".equals(YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("STATUS")))?CodeUtil.getCodeName(ChargeBikeConstant.CODETYPE_Asset_YXZT,(String) loadList.get(i).get("STATUS")):"");
            maps.put("stationAddr", YunRuiCommonUtil.nullToEmpty((String) loadList.get(i).get("STATIONADDR")));   
            maps.put("deviceNo", loadList.get(i).get("deviceNo"));
			maps.put("csServiceRate",loadList.get(i).get("csServiceRate").toString()+"%");
            maps.put("minServiceMoney", loadList.get(i).get("minServiceMoney").toString());
			resultlist.add(maps);			  						 							  
		 }
		 resultMap.put("result", resultlist);
		 JsonConfig json=new JsonConfig();
		 return JSONArray.fromObject(resultMap, json).toString();
		
	 }
	 @RequestMapping(value="/fwfAdd.htm",produces="text/plain;charSet=UTF-8")
	 public @ResponseBody String fwfAdd(HttpServletRequest request)
	 {
		 boolean result=false;
		 String stationId=request.getParameter("stationId");
		 String stationfwf=request.getParameter("stationfwf");
		 String minServiceMoney=request.getParameter("minServiceMoney");
		 result=chargeStationMtcService.fwfAdd(stationId,stationfwf,minServiceMoney);
		 return "{\"result\":"+result+"}";
	 }
}
