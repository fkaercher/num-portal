#!/bin/bash

### Note install jq ibrary from here https://stedolan.github.io/jq/download/

fhirBridgeBaseURL=https://fhir-bridge.crr.pre-prod.num-codex.de/fhir-bridge
openEHRRestAPIBase=https://ehrbase.crr.pre-prod.num-codex.de/ehrbase/rest/openehr/v1
ehrbaseUsername=ehrbaseAuth
ehrbasePassword=
pseudonyms=codex_9PJGLK,codex_FT2WN6,codex_KP08FK,codex_9PJGLK,codex_9T363Q,codex_WX6QAM,codex_R4AH8N,codex_5FTZER,codex_2MMWKK,codex_NC2PG0,codex_97QRYQ,codex_9R6Y20,codex_RP2XPR,codex_WF0WGJ,codex_K4UX31,codex_5LMCRW,codex_TE3CH2,codex_8NTU52,codex_YNH639,codex_Y9ADLK,codex_7PTG22,codex_451NG3,codex_3WL2RU,codex_90825J,codex_0UDGUP,codex_RP8T95,codex_FKUYU8,codex_FP4L50,codex_AARD17,codex_JQ1773,codex_QJWR6T,codex_4K80N5,codex_GJ87MT,codex_UDAEGG,codex_AYXAMY,codex_0AA0W1,codex_Q3XU7Y,codex_X6GN00,codex_2F0HAP,codex_H2KLUK,codex_QUYXN8,codex_ZY3248,codex_46D1DN,codex_RRQTHJ,codex_43AGPU,codex_T13PC4,codex_6UUF6F,codex_XFP1ZP,codex_H48201,codex_D6DREJ,codex_9DNH46,codex_XH3QKN,codex_J9R7RD,codex_ANLJFU,codex_7G2J0M,codex_5DRJQU,codex_L4WD0F,codex_PYM9EK,codex_X8L8D9,codex_Z5E8LD,codex_T7KW80,codex_JQJDX9,codex_Q3W2G1,codex_DKNDXF,codex_8K3C1J,codex_QRRHC3,codex_9NCGXR,codex_XFRH2Q,codex_ZHZRDA,codex_UWLQAY,codex_DJM6CH,codex_80ZQDY,codex_2R1DHG,codex_FZW3DJ,codex_M0W0QC,codex_6N88NN,codex_DJ7K9A,codex_712531,codex_MKF09W,codex_7GPQKR,codex_YA1LQC,codex_G55CKU,codex_WGA03J,codex_MZ05PD,codex_QPTNCM,codex_DXQHWC,codex_4ZJ8PF,codex_T9MTXU,codex_E1H0YK,codex_F64YGH,codex_ER5LTL,codex_85QZX6,codex_K80QFQ,codex_PQ79CY,codex_QKGJWX,codex_P67J62,codex_RY63AJ,codex_5M9CC5,codex_MFYDKM,codex_90W52L,codex_4Q75JP,codex_T322QD,codex_L6L6MQ,codex_WYA1CU,codex_AW0E8T,codex_53U91U,codex_WP7160,codex_XH3LHG,codex_WAUZ1W,codex_0DU418,codex_PWFECK,codex_37F22M,codex_1FL1XL,codex_3FEE0J,codex_P9XQEY,codex_WPNQ3W,codex_9U23NK,codex_JXLAM2,codex_LP5NTX,codex_1TNAAH,codex_GU3FJM,codex_W6QLCF,codex_F637HT,codex_W3N1MA,codex_8L3436,codex_4A2MUW,codex_48JQY7,codex_HPQ6P2,codex_0J0WA8,codex_3MKXWX,codex_XWF949,codex_EF4WW8,codex_TPY1TQ,codex_K22Y9L,codex_DT4FQL,codex_350LKT,codex_K3QMJK,codex_AGFL7Q,codex_EXPUR0,codex_T3PTTA,codex_44985N,codex_T1L4RM,codex_EQUWRC,codex_PGJK81,codex_TAMR4C,codex_P3YTAF,codex_Y0NCL8,codex_MNZRLC,codex_XRDH3U,codex_APWTQQ,codex_URFT91,codex_27K3GZ,codex_YU6HXH,codex_TM8NCX,codex_NRWQW7,codex_W1HMQC,codex_0ZP4PD,codex_UN6U5Y,codex_Q0XEF9,codex_X7JK0U,codex_EYRX75,codex_GC4G58,codex_HAXWLJ,codex_XJQWT1,codex_QEYKP6,codex_R0348Z,codex_A4JQ8Z,codex_75URMF,codex_W3U33F,codex_W58062,codex_DQ0T75,codex_0MJ0K6,codex_79UKC3,codex_Z5QMU7,codex_1DYYRW,codex_ZZ63M9,codex_F277D1,codex_T5QKLC,codex_QXP247,codex_T1CFDW,codex_40K7UR,codex_2KZRCA,codex_Y5P8Q1,codex_6X64RX,codex_LQ5MA4,codex_70M71M,codex_R7U4K0,codex_GP4J4N,codex_ERTD0G,codex_NP3EXH,codex_CDQXP9,codex_ZA7D6G,codex_Z1740H,codex_1ZZZ9T,codex_A63XPQ,codex_13M922,codex_X4E3E2,codex_DX2M33,codex_9PG2TQ,codex_TL9QY9,codex_L7224U,codex_4CLQNF,codex_U61DUE,codex_ZMLMHM,codex_7KG206,codex_LF5U21,codex_0U028U,codex_U0XJZT,codex_DF8TKL,codex_A9LYC6,codex_K8TGM8,codex_CJ7KLM,codex_2AWREE,codex_EWMD41,codex_WMYH3D,codex_L3YWZ2,codex_96DC33,codex_T5CZF3,codex_40AUYR,codex_E4HPDJ,codex_ZAJP84,codex_9F4M8N,codex_CZ4YND,codex_965PK7,codex_Z66Z8L,codex_HP3G6N,codex_WXKWJ0,codex_AQMXYH,codex_Y1F492,codex_A2WFCZ,codex_U6920Y,codex_GX05C1,codex_C2ZZMC,codex_015EGA,codex_ZQ7KR4,codex_5WUY18,codex_LH1E4M,codex_N8ZREP,codex_YTRLXN,codex_QMYR7R,codex_5CQYZ6,codex_0F1Q8F,codex_76MJ24,codex_E5WNGX,codex_882H4D,codex_8GDF0K,codex_JFFA7Z,codex_HGYCQR,codex_RH29KY,codex_3QG7PZ,codex_TMYHKT,codex_AHDN0F,codex_ELCP7C,codex_72EPLT,codex_147NCA,codex_96GY8F,codex_AMCJ7R,codex_ZHTK7W,codex_CNZWRT,codex_NUNAKT,codex_719NA4,codex_TUEYCM,codex_DXDR13,codex_WELAX3,codex_ZQ79YH,codex_Y17XQF,codex_3GULAM,codex_KLHW9H,codex_MMKN0Y,codex_WHZCJZ,codex_LWYA8F,codex_F7CQUY,codex_96JJRX,codex_G4GCP4,codex_RUL33P,codex_Z6WUF3,codex_1E2KHX,codex_9EJ48T,codex_7GPQWG,codex_Y7X7FF,codex_P2G1L0,codex_D7JW08,codex_H41T0H,codex_HD1F75,codex_J4MFDM,codex_WWT73R,codex_Z4ZKHL,codex_YAXR03,codex_20U9PL,codex_UF46DL,codex_99X1NT,codex_7GGCCP,codex_5CY410,codex_C9MHRG,codex_H5T8QU,codex_KW5XEW,codex_DG8N86,codex_2A1QJJ,codex_633U07,codex_FG36KG,codex_NNKLA1,codex_0G124M,codex_T361UJ,codex_RDR65C,codex_7ZX2YD,codex_8NEYQ4,codex_Z19NJU,codex_08RGGH,codex_62DMZN,codex_XWC11A,codex_UC3W6Z,codex_H4RLPL,codex_5HFU6Y,codex_PRX9FF,codex_JW1AD9,codex_9THQ0N,codex_7QJ0K5,codex_CY3JR0,codex_6AQNW7,codex_4JWFU6,codex_RMLYNN,codex_RU516E,codex_5AGLP3,codex_UM929D,codex_NNZCRH,codex_0UWEWF,codex_X94XAE,codex_6X74TR,codex_2X0LG0,codex_QFY4ZT,codex_588924,codex_QA68U8,codex_P45WFL,codex_W1632N,codex_XJ73UQ,codex_8E79FC,codex_17CLJC,codex_EA59PW,codex_WU4D7K,codex_EXFKGM,codex_3LCM2K,codex_Z474C2,codex_QZTQM6,codex_HWLA3P,codex_TEJRTH
subjectNamespace="fhir-bridge"
patientURL=${fhirBridgeBaseURL}/fhir/Patient
observationURL=${fhirBridgeBaseURL}/fhir/Observation
conditionURL=${fhirBridgeBaseURL}/fhir/Condition
procedureURL=${fhirBridgeBaseURL}/fhir/Procedure
diagnosticReportURL=${fhirBridgeBaseURL}/fhir/DiagnosticReport
procedureReportURL=${fhirBridgeBaseURL}/fhir/Procedure
medicationStatementURL=${fhirBridgeBaseURL}/fhir/MedicationStatement
immunizationURL=${fhirBridgeBaseURL}/fhir/Immunization
aqlQueryURL=${openEHRRestAPIBase}/query/aql
logToFile=true
if [ -z "${pseudonyms}" ]; then 
	echo "No pseudonyms provided";
	exit 1;
fi

function logInfo() {
	local identifier=$1
	local logEntry=$2
	if [ "$logToFile" = true ]; then
		echo $logEntry >> $identifier.log
	else
		echo $logEntry;
	fi	
}

for codexIdentifier in ${pseudonyms//,/ }
do
echo "start process pseudonym $codexIdentifier"
retrieveEHRBySubjectId="${openEHRRestAPIBase}/ehr?subject_id=$codexIdentifier&subject_namespace=$subjectNamespace"
aqlQueryBody="{ \"q\": \"Select c/uid/value, e/ehr_id/value, c/archetype_details/template_id/value, c/feeder_audit from EHR e contains composition c WHERE e/ehr_status/subject/external_ref/id/value = '$codexIdentifier'\" }"
# note fhir_resource_id is the id from fhir_bridge DB, table fb_resource_composition.resource_id

#Get patient resource id (the one stored in fhir-bridge)
resourceId=$(curl -X GET "${patientURL}?identifier=${codexIdentifier}" | jq '.entry[0].resource.id')
if [[ -z "$resourceId" || "${resourceId}" = 'null' ]]; then
	echo "No patient found for ${codexIdentifier}";
	continue; #skip the next requests 
fi
logEntry="patient with pseudonym $codexIdentifier has fhir_resource_id $resourceId;"
logInfo "$codexIdentifier" "$logEntry"
#Get/find corresponding EHR id for patient
ehrId=$(curl -u "$ehrbaseUsername:$ehrbasePassword" -X GET "$retrieveEHRBySubjectId" | jq '.ehr_id.value');
logEntry="Patient with pseudonym $codexIdentifier has EHR with id $ehrId";
logInfo "$codexIdentifier" "$logEntry"

findObservationResponse=$(curl -X GET "$observationURL?subject.identifier=$codexIdentifier");
logEntry=$(echo "total number of observations for patient $codexIdentifier is:" $(echo $findObservationResponse | jq '.total'))
logInfo "$codexIdentifier" "$logEntry"

logEntry=$(echo $findObservationResponse | jq '.entry[]? | {fullUrl: .fullUrl, fhir_resource_id: .resource.id, profile: .resource.meta.profile[0]}')
logInfo "$codexIdentifier" "$logEntry"

findConditionsResponse=$(curl -X GET "$conditionURL?subject.identifier=$codexIdentifier");
logEntry=$(echo "total number of conditions for patient $codexIdentifier is:" $(echo $findConditionsResponse| jq '.total'))
logInfo "$codexIdentifier" "$logEntry"

logEntry=$(echo $findConditionsResponse | jq '.entry[]? | {fullUrl: .fullUrl, fhir_resource_id: .resource.id}')
logInfo "$codexIdentifier" "$logEntry"

findDiagnosticReportsResponse=$(curl -X GET "$diagnosticReportURL?subject.identifier=$codexIdentifier");
logEntry=$(echo "total number of diagnostic reports for patient $codexIdentifier is:" $(echo $findDiagnosticReportsResponse| jq '.total'))
logInfo "$codexIdentifier" "$logEntry"
logEntry=$(echo $findDiagnosticReportsResponse | jq '.entry[]? | {fullUrl: .fullUrl, fhir_resource_id: .resource.id}')
logInfo "$codexIdentifier" "$logEntry"

findProceduresResponse=$(curl -X GET "$procedureReportURL?subject.identifier=$codexIdentifier");
logEntry=$(echo "total number of procedures for patient $codexIdentifier is:" $(echo $findProceduresResponse| jq '.total'))
logInfo "$codexIdentifier" "$logEntry"
logEntry=$(echo $findProceduresResponse | jq '.entry[]? | {fullUrl: .fullUrl, fhir_resource_id: .resource.id}')
logInfo "$codexIdentifier" "$logEntry"

findMedicationsResponse=$(curl -X GET "$medicationStatementURL?subject.identifier=$codexIdentifier");
logEntry=$(echo "total number of medications for patient $codexIdentifier is:" $(echo $findMedicationsResponse| jq '.total'))
logInfo "$codexIdentifier" "$logEntry"
logEntry=$(echo $findMedicationsResponse | jq '.entry[]? | {fullUrl: .fullUrl, fhir_resource_id: .resource.id}')
logInfo "$codexIdentifier" "$logEntry"

findImmunizationsResponse=$(curl -X GET "$immunizationURL?patient.identifier=$codexIdentifier");
logEntry=$(echo "total number of immunizations for patient $codexIdentifier is:" $(echo $findImmunizationsResponse| jq '.total'))
logInfo "$codexIdentifier" "$logEntry"
logEntry=$(echo $findImmunizationsResponse | jq '.entry[]? | {fullUrl: .fullUrl, fhir_resource_id: .resource.id}')
logInfo "$codexIdentifier" "$logEntry"

selectCompositionsResponse=$(curl -X POST "$aqlQueryURL" -u "$ehrbaseUsername:$ehrbasePassword" -H "Content-Type: application/json" -d "${aqlQueryBody}");
logEntry=$(echo "total number of compositions for pseudonym $codexIdentifier is:" $(echo $selectCompositionsResponse| jq '.rows | length'))
logInfo "$codexIdentifier" "$logEntry"
logEntry=$(echo $selectCompositionsResponse | jq '.rows[]? | {compositionId: .[0], ehr_id: .[1], template: .[2]}')
logInfo "$codexIdentifier" "$logEntry"
echo "end process pseudonym $codexIdentifier"
done;