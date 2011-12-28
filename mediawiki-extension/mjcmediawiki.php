<?php
/* Copyright (c) 2011 F.A.Loo <fa_loo@yahoo.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


$wgExtensionFunctions[] = 'efJiraExportInit';

function efJiraExportInit() {
	global	$wgParser;
	$wgParser->setHook( 'jiraexport', 'efJiraExportRender' );
}
 
function efJiraExportRender( $input, $args, $parser ) {
	$parser->disableCache();
	global	$JIRAs, $JIRAdefault;
	try {			
		$which = $JIRAdefault;
		$jira = new SoapClient($JIRAs[$which]['urllootest']);
		$auth = $jira->login($JIRAs[$which]['user'], $JIRAs[$which]['password']);
		
		$info = $jira->getServerInfo($auth);
		$url = $info->baseUrl;
		$issues = $jira->getIssuesFromJqlSearch($auth, $input, 1000);
		$issues = array_reverse($issues);
		$issuestatuses = $jira->getStatuses($auth);
		$issuepriorities = $jira->getPriorities($auth);
		$issueresolutions = $jira->getResolutions($auth);
		
		$s = "";
		$number = 1;
		
		foreach ($issues as $issue) {
			$issuekey = $issue->key;
			$issuesummary = $issue->summary;
			$issuedescription = $issue->description;
			
			//$issuecomponent = ($issue->components)->name;
			$issuecomponentary = array();
			$issuecomponentobjectarray = $issue->components;
			foreach ($issuecomponentobjectarray as $issuecomponentobjectitm) {
				$issuecomponentary[] = $issuecomponentobjectitm->name;
			}
			//$issuecomponent = print_r($issuecomponentary, true);
			$issuecomponent = implode(", ", $issuecomponentary);
			
			//$issueaffectversion = ($issue->affectsversions)->name;
			$issueaffectversionary = array();
			$issueaffectversionobjectarray = $issue->affectsVersions;
			foreach ($issueaffectversionobjectarray as $issueaffectversionobjectitm) {
				$issueaffectversionary[] = $issueaffectversionobjectitm->name;
			}
			//$issueaffectversion = print_r($issueaffectversionary, true);
			$issueaffectversion = implode(", ", $issueaffectversionary);
			
			//$issuefixversion = ($issue->fixversions)->name;
			$issuefixversionary = array();
			$issuefixversionobjectarray = $issue->fixVersions;
			foreach ($issuefixversionobjectarray as $issuefixversionobjectitm) {
				$issuefixversionary[] = $issuefixversionobjectitm->name;
			}
			//$issuefixversion = print_r($issuefixversionary, true);
			$issuefixversion = implode(", ", $issuefixversionary);
			
			$issueassignee = $issue->assignee;
			$issuereporter = $issue->reporter;
			$issueresolutionid = $issue->resolution;
			$issuestatusid = $issue->status;
			$issuepriorityid = $issue->priority;
			$issueproject = $issue->project;
			$issuecreated = $issue->created;
			$issueduedate = $issue->duedate;
			$issueupdated = $issue->updated;
			$issueenvironment = $issue->environment;
			$issuecustomfieldvaluesarray = $issue->customFieldValues;
			
			$userCustomFieldDataArray = array();
			
			foreach ($issuecustomfieldvaluesarray as $customfieldobject) {
				$userCustomFieldId = $customfieldobject->customfieldId;
				$userCustomFieldName = $jira->getCustomFieldNameFromId($auth, $userCustomFieldId);
				
				$userCustomFieldValueArray = $customfieldobject->values;
				$userCustomFieldValueString = implode(", ", $userCustomFieldValueArray);
				
				
				unset($customFieldDataArray);
				$customFieldDataArray = array(
					'userCustomFieldId' => $userCustomFieldId,
					'userCustomFieldName' => $userCustomFieldName,
					'userCustomFieldValue' => $userCustomFieldValueString);
				
				$userCustomFieldDataArray[] = $customFieldDataArray;
				
			}
			
			$dataarray = array(
				'issuekey' => $issuekey, 
				'issuesummary' => $issuesummary,
				'issuedescription' => $issuedescription,
				'issueassignee' => $issueassignee, 
				'issuereporter' => $issuereporter,
				'issueresolutionid' => $issueresolutionid,
				'issuestatusid' => $issuestatusid, 
				'issuepriorityid' => $issuepriorityid,
				'issueproject' => $issueproject,
				'issuecreated' => $issuecreated, 
				'issueduedate' => $issueduedate,
				'issueupdated' => $issueupdated,
				'issueenvironment' => $issueenvironment,
				'issuecomponent' => $issuecomponent,
				'issueaffectversion' => $issueaffectversion,
				'issuefixversion' => $issuefixversion,
				'issuecustomfieldvaluesarrayfromissue' => $issuecustomfieldvaluesarrayfromissue,
				'userCustomFieldDataArray' => $userCustomFieldDataArray,
				'issuestatuses' => $issuestatuses,
				'issuepriorities' => $issuepriorities,
				'issueresolutions' => $issueresolutions);
				
			$AttachmentsFromIssue = $jira->getAttachmentsFromIssue($auth, $issuekey);
			
			$attachmentURLarray = array();
			$attachmentNonImgURLarray = array();
			
			if (!empty($AttachmentsFromIssue)) {
				foreach ($AttachmentsFromIssue as $attfromissue) {
					$attachmentfilename = $attfromissue->filename;
					$attachmentID = $attfromissue->id;
					//$attachmentID = $jira->getAttachmentIdFromIssueKeyAndName($auth, $issuekey, $attachmentfilename);
					
					$fe = checkFileExtension($attachmentfilename);
					
					if ($fe == TRUE) {
						$attachmentURLarray[] = $url . '/secure/attachment/' . $attachmentID . '/' . $attachmentfilename;
					} else {
						$attachmentNonImgURLarray[] = $url . '/secure/attachment/' . $attachmentID . '/' . $attachmentfilename;
					}
				}
			}
			$s .= efJiraExportRenderIssue($issue, $dataarray, $attachmentURLarray, $attachmentNonImgURLarray, $number);
			$number++;
			
		}
		return $s;
		
	} catch (Exception $e) {
		return htmlspecialchars($e->getMessage());
		
	}
}

function checkFileExtension($fileName) {
	$parts = explode(".", $fileName);
	if (!empty($parts)) {
		$extname = $parts[count($parts)-1];
		if (($extname == "jpg") || ($extname == "png") || ($extname == "bmp") || ($extname == "jpeg") || ($extname == "gif")) {
			return TRUE;
		} else {
			return FALSE;
		}
	} else {
		return FALSE;
	}
}

function findObjectFromId($objects, $id) {
	foreach ($objects as $object) {
		if ($object->id === $id)
			return $object;
	}

	return NULL;
}

function efJiraExportRenderIssue($issue, $dataarray, $attachmentURLarray, $attachmentNonImgURLarray, $number) {

	$issuekey = $dataarray['issuekey'];
	$issuesummary = $dataarray['issuesummary'];
	$issuedescription = htmlspecialchars($dataarray['issuedescription']);
	$issueassignee = $dataarray['issueassignee'];
	$issuereporter = $dataarray['issuereporter'];
	$issueresolutionid = $dataarray['issueresolutionid'];
	$issuestatusid = $dataarray['issuestatusid'];
	$issuepriorityid = $dataarray['issuepriorityid'];
	$issueproject = $dataarray['issueproject'];
	$issuecreated = $dataarray['issuecreated'];
	$issueduedate = $dataarray['issueduedate'];
	$issueupdated = $dataarray['issueupdated'];
	$issueenvironment = $dataarray['issueenvironment'];
	$issuestatuses = $dataarray['issuestatuses'];
	$issuepriorities = $dataarray['issuepriorities'];
	$issueresolutions = $dataarray['issueresolutions'];
	$issuecomponent = $dataarray['issuecomponent'];
	$issueaffectversion = $dataarray['issueaffectversion'];
	$issuefixversion = $dataarray['issuefixversion'];
	$issuecustomfieldvaluesarrayfromissue = $dataarray['issuecustomfieldvaluesarrayfromissue'];
	$userCustomFieldDataArray = $dataarray['userCustomFieldDataArray'];
	
	$issuestatusobject = findObjectFromId($issuestatuses, $issuestatusid);
	if (!empty($issuestatusobject)) {
		$issuestatus = htmlspecialchars($issuestatusobject->name);
	} else {
		$issuestatus = "";
	}
	$issuepriorityobject = findObjectFromId($issuepriorities, $issuepriorityid);
	if (!empty($issuepriorityobject)) {
		$issuepriority = htmlspecialchars($issuepriorityobject->name);
	} else {
		$issuepriority = "";
	}
	$issueresolutionobject = findObjectFromId($issueresolutions, $issueresolutionid);
	if (!empty($issueresolutionobject)) {
		$issueresolution = htmlspecialchars($issueresolutionobject->name);
	} else {
		$issueresolution = "";
	}

	$s = "<table border='1' cellspacing='0' class='wikitable' style='width:95%'>";

	$s .= <<<_END
<tr>
	<td style="width:15%">Number:</td>
	<td style="width:85%">$number</td>
</tr>
<tr>
	<td style="width:15%">Issue Key:</td>
	<td style="width:85%">$issuekey</td>
</tr>
<tr>
	<td style="width:15%">Summary:</td>
	<td style="width:85%">$issuesummary</td>
</tr>
<tr>
	<td style="width:15%">Description:</td>
	<td style="width:85%">$issuedescription</td>
</tr>
<tr>
	<td style="width:15%">Assignee:</td>
	<td style="width:85%">$issueassignee</td>
</tr>
<tr>
	<td style="width:15%">Reporter:</td>
	<td style="width:85%">$issuereporter</td>
</tr>
<tr>
	<td style="width:15%">Resolution:</td>
	<td style="width:85%">$issueresolutionid - $issueresolution</td>
</tr>
<tr>
	<td style="width:15%">Status:</td>
	<td style="width:85%">$issuestatusid - $issuestatus</td>
</tr>
<tr>
	<td style="width:15%">Priority:</td>
	<td style="width:85%">$issuepriorityid - $issuepriority</td>
</tr>
<tr>
	<td style="width:15%">Project:</td>
	<td style="width:85%">$issueproject</td>
</tr>
<tr>
	<td style="width:15%">Components:</td>
	<td style="width:85%">$issuecomponent</td>
</tr>
<tr>
	<td style="width:15%">Affects Versions:</td>
	<td style="width:85%">$issueaffectversion</td>
</tr>
<tr>
	<td style="width:15%">Fix Versions:</td>
	<td style="width:85%">$issuefixversion</td>
</tr>
<tr>
	<td style="width:15%">Created:</td>
	<td style="width:85%">$issuecreated</td>
</tr>
<tr>
	<td style="width:15%">Due Date:</td>
	<td style="width:85%">$issueduedate</td>
</tr>
<tr>
	<td style="width:15%">Updated:</td>
	<td style="width:85%">$issueupdated</td>
</tr>
<tr>
	<td style="width:15%">Environment:</td>
	<td style="width:85%">$issueenvironment</td>
</tr>
_END;


	foreach ($userCustomFieldDataArray as $userCustomFieldDataItm) {
		if ($userCustomFieldDataItm['userCustomFieldName'] == "Actual Result") {
			$issueactualresult = $userCustomFieldDataItm['userCustomFieldValue'];
			$s .= <<<_END
<tr>
	<td style="width:15%">Actual Result:</td>
	<td style="width:85%">$issueactualresult</td>
</tr>
_END;
		}
		
		if ($userCustomFieldDataItm['userCustomFieldName'] == "Expected Result") {
			$issueexpectedresult = $userCustomFieldDataItm['userCustomFieldValue'];
			$s .= <<<_END
<tr>
	<td style="width:15%">Expected Result:</td>
	<td style="width:85%">$issueexpectedresult</td>
</tr>
_END;
		}
		
		if ($userCustomFieldDataItm['userCustomFieldName'] == "Root Cause") {
			$issuerootcause = $userCustomFieldDataItm['userCustomFieldValue'];
			$s .= <<<_END
<tr>
	<td style="width:15%">Root Cause:</td>
	<td style="width:85%">$issuerootcause</td>
</tr>
_END;
		}
		
		if ($userCustomFieldDataItm['userCustomFieldName'] == "Recommended Solution") {
			$issuerecommendedsolution = $userCustomFieldDataItm['userCustomFieldValue'];
			$s .= <<<_END
<tr>
	<td style="width:15%">Recommended Solution:</td>
	<td style="width:85%">$issuerecommendedsolution</td>
</tr>
_END;
		}
		
	}
	

	$s .= <<<_END
<tr>
	<td style="width:15%">Attachments:</td>
	<td style="width:85%">
_END;
	
	if (!empty($attachmentNonImgURLarray)) {
	
		foreach ($attachmentNonImgURLarray as $URL2) {
			$s .= "<br/><a href='$URL2'>$URL2</a><br/>";
		}
	}

	if (!empty($attachmentURLarray)) {
	
		foreach ($attachmentURLarray as $URL) {
			$s .= "<br/><img src='$URL' alt='$URL' title='$URL' style='max-width:90%; border:2px solid black'/><br/><br/>";
		}
	}
	
	$s .= "</td></tr></table><br/>";

	return $s;
}

?>
