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
		$s = "";
		
		foreach ($issues as $issue) {
			$issuekey = $issue->key;
			$issuesummary = $issue->summary;
			$issuedescription = $issue->description;
			$issueassignee = $issue->assignee;
			$issuereporter = $issue->reporter;
			$issueresolution = $issue->resolution;
			$issuestatus = $issue->status;
			$issuepriority = $issue->priority;
			$issueproject = $issue->project;
			$issuecreated = $issue->created;
			$issueduedate = $issue->duedate;
			$issueupdated = $issue->updated;
			$issueenvironment = $issue->environment;
			
			$dataarray = array(
				'issuekey' => $issuekey, 
				'issuesummary' => $issuesummary,
				'issuedescription' => $issuedescription,
				'issueassignee' => $issueassignee, 
				'issuereporter' => $issuereporter,
				'issueresolution' => $issueresolution,
				'issuestatus' => $issuestatus, 
				'issuepriority' => $issuepriority,
				'issueproject' => $issueproject,
				'issuecreated' => $issuecreated, 
				'issueduedate' => $issueduedate,
				'issueupdated' => $issueupdated,
				'issueenvironment' => $issueenvironment);
				
			$AttachmentsFromIssue = $jira->getAttachmentsFromIssue($auth, $issuekey);
			
			$attachmentURLarray = array();
			$attachmentNonImgURLarray = array();
			
			if (!empty($AttachmentsFromIssue)) {
				foreach ($AttachmentsFromIssue as $attfromissue) {
					$attachmentfilename = $attfromissue->filename;
					$attachmentID = $jira->getAttachmentIdFromIssueKeyAndName($auth, $issuekey, $attachmentfilename);
					
					if ($attachmentID != "FALSE") {
						$fe = checkFileExtension($attachmentfilename);
						
						if ($fe == TRUE) {
							$attachmentURLarray[] = $url . '/secure/attachment/' . $attachmentID . '/' . $attachmentfilename;
						} else {
							$attachmentNonImgURLarray[] = $url . '/secure/attachment/' . $attachmentID . '/' . $attachmentfilename;
						}
					}
				}
			}
			$s .= efJiraExportRenderIssue($issue, $dataarray, $attachmentURLarray, $attachmentNonImgURLarray);
			
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

function efJiraExportRenderIssue($issue, $dataarray, $attachmentURLarray, $attachmentNonImgURLarray) {

	$issuekey = $dataarray['issuekey'];
	$issuesummary = $dataarray['issuesummary'];
	$issuedescription = $dataarray['issuedescription'];
	$issueassignee = $dataarray['issueassignee'];
	$issuereporter = $dataarray['issuereporter'];
	$issueresolution = $dataarray['issueresolution'];
	$issuestatus = $dataarray['issuestatus'];
	$issuepriority = $dataarray['issuepriority'];
	$issueproject = $dataarray['issueproject'];
	$issuecreated = $dataarray['issuecreated'];
	$issueduedate = $dataarray['issueduedate'];
	$issueupdated = $dataarray['issueupdated'];
	$issueenvironment = $dataarray['issueenvironment'];

	$s = "<table border='1' cellspacing='0' class='wikitable' style='width:95%'>";

	$s .= <<<_END
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
	<td style="width:85%">$issueresolution</td>
</tr>
<tr>
	<td style="width:15%">Status:</td>
	<td style="width:85%">$issuestatus</td>
</tr>
<tr>
	<td style="width:15%">Priority:</td>
	<td style="width:85%">$issuepriority</td>
</tr>
<tr>
	<td style="width:15%">Project:</td>
	<td style="width:85%">$issueproject</td>
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
