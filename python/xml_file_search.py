class myfile:
	def __init__(self):
		self.class_simple = ""
		self.package = ""
		self.layer = ""

#['src-gen', 'TASKPHY', 'DB', 'TASKNET', 'UTIL', 'BLL', 'PHY', 'UI', 'NET', 'OPEN', 'gen', 'CONSTANTS', 'LOG']
# the layer of 'gen','src-gen','CONSTANTS','OPEN','LOG','rubbish' should be ignored
layer_ignore_set = ('src','DB-gen','LOG')
import collections
# use collections.defaultdict()
# layer_package_dict_set = ['UI':['com.espressif.iot.achartengine','com.espressif.iot.application',...]
#							, 'NET':['com.espressif.iot.net','com.espressif.iot.net.lan.wifi',...], 
#							...]
#
# package_class_dict_set = ['com.espressif.iot.achartengine':['AbstractChart','IChart',...]
#							, 'com.espressif.iot.application':['EspressifApplication',...]
#							...]
#

# one to many relation for layer and package
layer_package_dict_set = collections.defaultdict(set)
# one to many relation for package and class
package_class_dict_set = collections.defaultdict(set)

#../UI/com/espressif/iot/achartengine/AbstractChart.java
# valid_str = UI/com/espressif/iot/achartengine/AbstractChart.java
# layer = UI
# package = com.espressif.iot.achartengine
# class_simple = AbstractChart
#
# class_ = com.espressif.iot.achartengine.AbstractChart.class
def search(folder,filter,allfile):
	import os
	UNUSED_HEAD_LEN = 3# the len of "../"
	UNUSED_TAIL_LEN = 5# the len of ".java"
	folders = os.listdir(folder)
	for name in folders:
		# join folder and name together
		curname = os.path.join(folder,name)
		# check whether the curname is a file
		isfile = os.path.isfile(curname)
		if isfile:
			ext = os.path.splitext(curname)[1]
			count = filter.count(ext)
			if count>0:
				cur = myfile()
				# valid_str = 'UI/com/espressif/iot/achartengine/AbstractChart.java'
				valid_str = curname[UNUSED_HEAD_LEN:]
				# list_valid_str = ['UI','com','espressif','iot','achartengine','AbstractChart.java']
				list_valid_str = valid_str.split('/')
				len_list = len(list_valid_str)
				#print("valid_str="+valid_str)
				# cur.layer = 'UI'
				cur.layer = list_valid_str[0] + '/' + list_valid_str[1]
				#print("cur.layer="+cur.layer)
				# cur.class_simple = 'AbstractChart'
				cur.class_simple = list_valid_str[len_list-1][:(-1*UNUSED_TAIL_LEN)]
				#print("cur.class_simple="+cur.class_simple)
				# cur.package = com/espressif/iot/achartengine
				cur.package = valid_str[(len(cur.layer)+1):(-1*(len(cur.class_simple)+UNUSED_TAIL_LEN+1))]
				# cur.package = com.espressif.iot.achartengine
				cur.package = cur.package.replace('/','.')
				#print("cur.package="+cur.package)
				allfile.add(cur)
		else:
			search(curname,filter,allfile)
	return allfile

def filter_files(allfile):
	print("filter_files()")
	delete_files = set()
	# delete the myfile of layer_ignore_set
	for myfile in allfile:
		test_set = set()
		test_set.add(myfile.layer)
#print(str(test_set))
		if test_set.issubset(layer_ignore_set):
			#print("delete")
			delete_files.add(myfile)

	# delete the myfile which related .java don't have "org.apache.log4j.Logger" or file name has "qrode"
	# for if you want to use log4j, "import org.apache.log4j.Logger;" should be in source
	# but sometimes, there may be some space in it 
	#print("TODO delete .java which don't have org.apache.log4j.Logger")
	# file is "../layer/package(replace('.','/'))/class_simple.java"
	for myfile in allfile:
		file_name = "../" + myfile.layer + "/" + myfile.package.replace('.','/') \
					+ "/" + myfile.class_simple + ".java"
		#print("file_name:" + file_name)
		file_f = open(file_name,"r")
		file_str = file_f.read()
		file_f.close()
		if file_str.find('org.apache.log4j.Logger') == -1:
			delete_files.add(myfile)
		if file_name.find('qrcode') <> -1:
			delete_files.add(myfile)
	    # delete the file related 'qrcode'
#elif file_str.find('qrcode') <> -1:
#			delete_files.add(myfile)
	# do the delete action
	allfile -= delete_files
	return allfile

def gen_dict_set(allfile):
	print("gen_dict_set()")
	global layer_package_dict_set
	global package_class_dict_set
	for myfile in allfile:
		layer_package_dict_set[myfile.layer].add(myfile.package)
		package_class_dict_set[myfile.package].add(myfile.class_simple)
	print(list (layer_package_dict_set))
#print(layer_package_dict_set.items())

def generate(allfile,xml):
	import xml.dom.minidom
	global layer_package_dict_set
	global package_class_dict_set

	impl = xml.dom.minidom.getDOMImplementation()
	dom = impl.createDocument(None, "log4j", None)
	node_log4j = dom.documentElement

	# log4j set level attribute
	node_log4j.setAttribute('level','DEBUG')
	# log4j add layer node
	list_layer = list(layer_package_dict_set)
	print("list_layer:"+str(list_layer))
	for layer in list_layer:
		node_layer = dom.createElement("layer")
		node_layer.setAttribute('content',layer)
		node_layer.setAttribute('level','IGNORE')
		list_package = layer_package_dict_set[layer]
		for package in list_package:
			#print("package:"+str(package))
			node_package = dom.createElement("package")
			node_package.setAttribute('content',package)
			node_package.setAttribute('level','IGNORE')
			list_class = package_class_dict_set[package]
			for class_simple in list_class:
#node_class = dom.createElement("class:"+class_simple)
				node_class = dom.createElement("class")
				node_class.setAttribute('content',class_simple)
				node_class.setAttribute('level','IGNORE')
				node_package.appendChild(node_class)
			node_layer.appendChild(node_package)
		node_log4j.appendChild(node_layer)

#print('xml='+xml)
	f=open('log4j.xml','w')
	dom.writexml(f,addindent='    ',newl = '\n',encoding='utf-8')
	f.close()


#from xml.dom import minidom
#doc = xml.dom.minidom.getDOMImplementation()

#root = doc.createElement("root")
#doc.appendChild(root)

#for myfile in allfile:
#file = doc.createElement("file")
#root.appendChild(file)

#name = doc.createElement("name")
#file.appendChild(name)
#namevalue = doc.createTextNode(myfile.name)
#name.appendChild(namevalue)

#print doc.toprettyxml(indent="")
#f = open(xml,'a+')
#f.write(doc.toprettyxml(indent=""))
#f.close()

filter = [".java"]
allfile = set()
folder = "../java"
print folder
allfile = search(folder,filter,allfile)
len = len(allfile)
print "found:" + str(len) + " files"
xml = 'log4j.xml'
allfile = filter_files(allfile)
gen_dict_set(allfile)
generate(allfile,xml)
