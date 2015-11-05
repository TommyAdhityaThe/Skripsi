<?php
	// Convert tracks from database into text file to be read by the mjnserve
	require_once '../../etc/utils.php';
	require_once '../../etc/constants.php';
	
	header('Content-Type: text/plain');
	header('Cache-control: no-cache');
	header('Pragma: no-cache');

	init_mysql();

	// Retrieve the maximum distance between 2 nodes.
	$maxdistance = $_GET['maxdistance'];
	if (!is_numeric($maxdistance)) {
		$maxdistance = 0;
	}
	
	$result = mysqli_query($global_mysqli_link, "SELECT propertyvalue FROM properties WHERE propertyname='trackversion'") or
		die_nice("Can't retrieve the tracks version from database: " . mysqli_error($global_mysqli_link));
	if ($row = mysqli_fetch_row($result)) {
		$trackversion = $row[0];
	} else {
		$trackversion = "UNKNOWN";
	}	
	
	if ($_SERVER['REMOTE_ADDR'] != '127.0.0.1') {
		print("Forbidden! Access only from localhost!\n");
		exit(0);
	}
	print "# gettracks.php result, trackversion=$trackversion\n";
	if ($maxdistance != 0) {
		print "# will extract node pairs longer than $maxdistance km.\n";
	}
	print "# Format: <trackId>\t<penalty>\t<num-of-points>\t<lat1 lon1 lat2 lon2>\t<loop>\t<transferNodes>\n";
	$result = mysqli_query($global_mysqli_link, "SELECT trackTypeId, trackId, AsText(geodata), pathloop, penalty, transferNodes FROM tracks WHERE geodata IS NOT NULL ORDER BY trackId") or	
		die_nice("Can't retrieve the track infromation from database: " . mysqli_error($global_mysqli_link));
	while ($row = mysqli_fetch_row($result)) {
		if (!file_exists('../../public_html/images/means/' . $row[0] . '/' . $row[1] . $means_image_extension)) {
			print "# WARNING: couldn't find image file for this route: " . $row[0] . '.' . $row[1] . "\n";
		}
		// Setup track info
		$tracks = lineStringToLatLngArray($row[2]);
		$tracksize = sizeof($tracks);
		$transitnodes = $row[5] == '' ? array('0-' . ($tracksize - 1)) : split(',', $row[5]);
		$transitnodes_count = count($transitnodes);
		for ($i = 0; $i < $transitnodes_count; $i++) {
			$transitnodes[$i] = rangestr_to_array($transitnodes[$i]);
		}
		$trackstring = '';
		$inserted_nodes = 0;
		$transfernodes_offset = array();
		// Print tracks
		for ($i = 0, $length = sizeof($tracks); $i < $length; $i++) {
			// Process the list
			list($lat, $lon) = explode(',', $tracks[$i]);
			// several actions if we are higher than 1st element 
			if ($i > 0) {
				// first, add a separator
				$trackstring .= ' ';
				// check if we are in transit nodes.
				$in_transitnode = false;
				for ($j = 0; $j < $transitnodes_count; $j++) {
					if ($i >= $transitnodes[$j][0] && $i <= $transitnodes[$j][1]) {
						$in_transitnode = true;
					}
				}
				// then, check if we have to add virtual nodes.				
				if ($maxdistance != 0 && ($distance = compute_distance($lat, $lon, $prevlat, $prevlon)) > $maxdistance && $in_transitnode) {
					// add virtual nodes now.
					$extranodes = ceil($distance / $maxdistance) - 1;
					for ($j = 1; $j <= $extranodes; $j++) {
						$extralat = $prevlat + $j * ($lat - $prevlat) / $extranodes;
						$extralon = $prevlon + $j * ($lon - $prevlon) / $extranodes;
						$trackstring .= sprintf("%.$latlon_precision" . "f %.$latlon_precision" . "f ", $extralat, $extralon);
					}
					$tracksize += $extranodes;
					$inserted_nodes += $extranodes;
				}
			}
			$transfernodes_offset[$i] = $inserted_nodes;
			$trackstring .= "$lat $lon";
			$prevlat = $lat; $prevlon = $lon;
		}
		for ($i = 0; $i < $transitnodes_count; $i++) {
			// Adjust with offset
			for ($j = 0; $j < 2; $j++) {
				$transitnodes[$i][$j] += $transfernodes_offset[$transitnodes[$i][$j]];
			}
			$transitnodes[$i] = array_to_rangestr($transitnodes[$i]);
		}
		print $row[0] . '.' . $row[1] . "\t" . $row[4] . "\t" . $tracksize . "\t" . $trackstring . "\t" . $row[3] . "\t" . join(',', $transitnodes) . "\n";  
	}

	deinit_mysql();
	
	/**
	 * Converts a number or range to an array of 2-element: start and finish.
	 * @param string $range_str the range in string, e.g. "1" or "2-3".
	 * @return the range in array format 
	 */
	function rangestr_to_array($range_str) {
		$list = split('-', $range_str);
		if (count($list) == 1) {
			return array(intval($list[0]), intval($list[0]));
		} else {
			return array(intval($list[0]), intval($list[1]));
		}
	}
	
	/**
	 * Converts 2-element array to a range format
	 * @param mixed $array the range in array format 
	 * @return the range in string, e.g. "1" or "2-3".
	 */
	function array_to_rangestr($array) {
		if ($array[0] == $array[1]) {
			return '' . $array[0];
		} else {
			return $array[0] . '-' . $array[1];
		}
	}
?>
