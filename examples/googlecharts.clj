; Copyright 2009 Jonathan A. Smith
; Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
; You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to
; in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
; either express or implied. See the License for the specific language governing permissions and limitations under the License. 

;; google charts api reference: http://code.google.com/apis/chart/ 

(ns google-chart [:require api-builder])	

  (api-builder/build-api{:root "chart.apis.google.com" 
			 :methods [{:name chart
				    :path ["chart"]
				    :params [:cht :chd :chs
					     :chl :chbh :chp
					     :chtm :chco :chld
					     :chf :choe :chm
					     :chtt :chdl :chdlp
					     :chxt :chxl :chxp
					     :chxr :chxs :chxtc
					     :chds :chma :chls
					     :chg]
				    :receive-type :image :type :get}]
			 :extension "?"})

(defn s-chart [params]
    (let [img (google-chart/chart params)
	  fr (new javax.swing.JFrame)
	  lb (new javax.swing.JLabel 
		  (new javax.swing.ImageIcon img) javax.swing.JLabel/CENTER)]
      (.. javax.swing.JOptionPane (showMessageDialog nil lb "icon" -1))))

(comment
;; to be pasted into repl
  (google-chart/s-chart 
   {:cht "t"
    :chs "440x220"
    :chld "DZEGMGAOBWNGCFKECGCVSNDJTZGHMZZM"
    :chtm "africa"
    :chco "FFF0FF,FF0000,FFFF00,00FF00"
    :chd "t:0,100,50,32,60,40,43,12,14,54,98,17,70,76,18,29"
    :chf "bg,s,EAF7FE"})

 (google-chart/s-chart {:cht "rs" 
	   :chs "200x200" :chd "s:voJATd9v,MW9BA9"
           :chco "FF0000,FF9900" 
           :chls "2.0,4.0,0.0|2.0,4.0,0.0" 
	   :chxt "x" 
	   :chxl "0:|0|45|90|135|180|225|270|315&chxr=0,0.0,360.0"
           :chg "25.0,25.0,4.0,4.0" 
           :chm "B,FF000080,0,1.0,5.0|B,FF990080,1,1.0,5.0|h,0000FF,0,1.0,4.0|h,3366CC80,0,0.5,5.0|V,00FF0080,0,1.0,5.0|V,008000,0,5.5,5.0|v,00A000,0,6.5,4"})

  (s-chart {:cht "rs"
	    :chs "200x200"
	    :chd"s:voJATd9v,MW9BA9&chco=FF0000,FF9900"
	    :chls "2.0,4.0,0.0|2.0,4.0,0.0" :chxt "x"
	    :chxl "0:|0|45|90|135|180|225|270|315" :chxr "0,0.0,360.0"
	    :chg "25.0,25.0,4.0,4.0"  
	    :chm "B,FF000080,0,1.0,5.0|B,FF990080,1,1.0,5.0|h,0000FF,0,1.0,4.0|h,3366CC80,0,0.5,5.0|V,00FF0080,0,1.0,5.0|V,008000,0,5.5,5.0|v,00A000,0,6.5,4"}))
