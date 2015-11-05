<?php
	// Convert tracks from database into text file to be read by the mjnserve
	require_once '../../etc/utils.php';
	require_once '../../etc/constants.php';

	header('Content-Type: text/plain');
	header('Cache-control: no-cache');
	header('Pragma: no-cache');
	
	if ($_SERVER['REMOTE_ADDR'] != '127.0.0.1') {
		print("Forbidden! Access only from localhost!\n");
		exit(0);
	}

	init_mysql();
	
	$result = mysqli_query($global_mysqli_link, "SELECT propertyvalue FROM properties WHERE propertyname='placesversion'") or
		die_nice("Can't retrieve the places version from database: " . mysqli_error($global_mysqli_link));
	if ($row = mysqli_fetch_row($result)) {
		$placesversion = $row[0];
	} else {
		$placesversion = "UNKNOWN";
	}
	
	print "# getplaces.php result, placesversion=$placesversion\n";
	print "# Format: <orderId>\t<lat>\t<lon>\n";
	$result = mysqli_query($global_mysqli_link, "SELECT orderId, AsText(location) FROM adorder WHERE location IS NOT NULL ORDER BY orderId") or	
		die_nice("Can't retrieve the places infromation from database: " . mysqli_error($global_mysqli_link));
	while ($row = mysqli_fetch_row($result)) {
		$latlng = preg_replace('/POINT\(-?(\d+(\.\d+)?) (-?\d+(\.\d+)?)\)/', "$3\t$1", $row[1]);
		print $row[0] . "\t$latlng\n";  
	}

	deinit_mysql();
?>