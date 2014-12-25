#!/bin/bash

export LC_ALL=C

echo 'Registration Number|Record Type|Content Indicator|File Number|Unique System Identifier|Coordinate Type|Latitude Degrees|Latitude Minutes|Latitude Seconds|Latitude Direction|Latitude_Total_Seconds|Longitude Degrees|Longitude Minutes|Longitude Seconds|Longitude Direction|Longitude_Total_Seconds|Record Type|Content Indicator|File Number|Unique System Identifier|Application Purpose|Previous Purpose|Input Source Code|Status Code|Date Entered|Date Received|Date Issued|Date Constructed|Date Dismantled|Date Action|Archive Flag Code|Version|Signature First Name|Signature Middle Initial|Signature Last Name|Signature Suffix|Signature Title|Invalid Signature|Structure_Street Address|Structure_City|Structure_State Code|||Height of Structure|Ground Elevation|Overall Height Above Ground|Overall Height AMSL|Structure Type|Date FAA Determination Issued|FAA Study Number|FAA Circular Number|Specification Option|Painting and Lighting|FAA EMI Flag|NEPA Flag' > asr-full.csv

# sort -t'|' -k4 CO.dat > CO-sorted.dat
# sort -t'|' -k4 RA.dat > RA-sorted.dat

join -t'|' -1 4 -2 4 CO-sorted.dat RA-sorted.dat | sed 's/||\r//g' >> asr-full.csv

echo 'ID,Latitude,Longitude,Height' > asr-tmp.csv
# cut -d'|' -f 5,11,16,46,24 asr.csv | sort -t'|' -k4 -r -g | sed 's/|/,/g' | uniq | head -111475 >> asr-min.csv

# $5: Unique System Identifier
# $11: Latitude
# $16: Longitude
# $24: T=Terminated, G=Granted, C=Constructed, A=Cancelled, I=Dismantled
# $46: Height
# $48: GTOWER / LTOWER / MAST / MTOWER / POLE / TANK / TOWER
awk -F'|' '{ if($24 == "C" && $11 != "" && $16 != "" && $16 != "0.0") print $5 "," $11 "," $16 "," $46 }' asr-full.csv | sort -t',' -k4 -r -g >> asr-tmp.csv

# Remove dups and trim
awk -F',' '{ if(a[$1]++ == 0 && $4 >= 60.9) print }' asr-tmp.csv > asr.csv

# GZip
gzip -c asr.csv > asr.csv.gz

# Upload to S3
s3cmd put asr.csv.gz s3://platypii/asr/
s3cmd setacl --acl-public s3://platypii/asr/asr.csv.gz
