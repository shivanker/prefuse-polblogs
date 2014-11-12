<?php
	
	$file="polbooks.gml";
	$target="polbooks.xml";
	
	$read=fopen($file,'r');
	$write=fopen($target,'w');
	
	$rep='';
	$node=true;
	
	while(!feof($read)){
		$line=fgets($read);

		if ($node)	{
			$close='</node>';
		}
		else 	{
			$close='/>';
		}
		$array1=array('/\s*node\s+\[?\s*/','/\s*id ([0-9]+)\s*/','/\s*edge\s+\[?\s*/','/\s*source ([0-9]+)\s*/','/\s*target ([0-9]+)\s*/','/\s*([a-z]+) "?([^"\n]*)"?\s*/','/\s*\]\s*/','/\s*\[\s*/');
		$array2=array('  <node'," id=\"$1\">\n",'  <edge',' source="$1"',' target="$1"',"    <data key=\"$1\">$2</data>\n",$close."\n");
		$rep=preg_replace($array1,$array2,$line);
		if (strcmp($rep,"<edge") > 0)	{
			$node=true;
		}
		elseif (strcmp($rep,"<node") < 0 && strcmp($rep,"<edge") >= 0) {
			$node=false;
		}
		fwrite($write,$rep);
	}
	$foot="\n</graph>\n</graphml>\n";
	fwrite($write,$foot);

	fclose($read);
	fclose($write);
?>
