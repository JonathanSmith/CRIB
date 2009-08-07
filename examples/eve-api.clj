; Copyright 2009 Jonathan A. Smith
; Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
; You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to
; in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
; either express or implied. See the License for the specific language governing permissions and limitations under the License. 

;; eve api reference: http://wiki.eve-id.net/APIv2_Page_Index
(ns eve-api [:require api-builder])

(defmacro utf8-encode 
  ([string]
     `(. java.net.URLEncoder (encode ~string "UTF-8")))
  ([string & replacements]
     (reduce (fn [x y] (if (symbol? y)
			 `(.replace ~x (first ~y) (second ~y))
			 `(let [a# ~y] (.replace ~x (first a#) (second a#))))) 
	     `(. java.net.URLEncoder (encode ~string "UTF-8"))
	     replacements)))

(let [ec (fn [s] (utf8-encode s ["+" "%20"]))]
  (api-builder/build-api 
   {:root "api.eve-online.com"
    :methods [{:name account :path ["account" "characters"] :params [:userid :apikey]}
				  
	      {:name server-status :path ["server" "ServerStatus"] :type :get}
				  
	      {:name map-jumps :path ["map" "jumps"] :type :get}
	      {:name map-kills :path ["map" "jumps"] :type :get}
	      {:name map-sov :path ["map" "sovereignty"] :type :get}
				  
	      {:name alliances :path ["eve" "allianceList"] :type :get}
	      {:name certificates :path ["eve" "certificateTree"] :type :get}
	      {:name errors :path ["eve" "ErrorList"] :params [] :type :get}
	      {:name fac-war-stats :path ["eve" "FacWarTopStats"] :type :get}
	      {:name id-name :path ["eve" "CharacterName"] :params [:ids] :type :get}
	      {:name name-id :path ["eve" "CharacterID"] :params [:names] :type :get}
	      {:name ref-types :path ["eve" "RefTypes"] :type :get}
	      {:name skills :path ["eve" "SkillTree"] :type :get}

	      {:name char-balance :path ["char" "AccountBalance"] :params [:userid :apikey :characterid] :type :get}
	      {:name char-assets :path ["char" "AssetList"] :params [:userid :apikey :characterid] :type :get}
	      {:name char-skills :path ["char" "CharacterSheet"] :params [:userid :apikey :characterid] :type :get}
	      {:name char-war-stats :path ["char" "FacWarStats"] :params [:userid :apikey :characterid] :type :get}
	      {:name char-indy-jobs :path ["char" "IndustryJobs"] :params [:userid :apikey :characterid] :type :get}
	      {:name char-kill-log :path ["char" "KillLog"] :params [:userid :apikey :characterid] :type :get}
	      {:name char-orders :path ["char" "MarketOrders"] :params [:userid :apikey :characterid] :type :get}
	      {:name char-medals :path ["char" "Medals"] :params [:userid :apikey :characterid] :type :get}
	      {:name training-skill :path ["char" "SkillInTraining"] :params [:userid :apikey :characterid] :type :get}
	      {:name skill-queue :path ["char" "SkillQueue"]  :params [:userid :apikey :characterid] :type :get}
	      {:name standings :path ["char" "Standings"]  :params [:userid :apikey :characterid] :type :get}
	      {:name char-journal :path ["char" "WalletJournal"]  :params [:userid :apikey :characterid] :type :get}
	      {:name char-trans :path ["char" "WalletTransactions"]  :params [:userid :apikey :characterid] :type :get}]
    :parser nil
    :encoder ec
    :extension ".xml.aspx?"}))
