; Copyright 2009 Jonathan A. Smith
; Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
; You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to
; in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
; either express or implied. See the License for the specific language governing permissions and limitations under the License. 

;;twitter api reference: http://apiwiki.twitter.com/Twitter-API-Documentation

(ns twitter-api 
  [:require api-builder
   clojure.contrib.json.read])

(defmacro utf8-encode 
  ([string]
     `(. java.net.URLEncoder (encode ~string "UTF-8")))
  ([string & replacements]
     (reduce (fn [x y] (if (symbol? y)
			 `(.replace ~x (first ~y) (second ~y))
			 `(let [a# ~y] (.replace ~x (first a#) (second a#))))) 
	     `(. java.net.URLEncoder (encode ~string "UTF-8"))

	     replacements)))

(let [rd clojure.contrib.json.read/read-json
      ec (fn [s] (utf8-encode s))]
  (api-builder/build-api 
   {:root "search.twitter.com"
    :methods [{:name search
	       :path ["search"]
	       :params [:q :callback :lang
			:rpp :page :since_id
			:geocode :show_user]
	       :type :get}
	      {:name trends
	       :path ["trends"]
	       :params []
	       :type :get}
	      {:name current-trends
	       :path ["trends" "current"]
	       :params [:exclude]
	       :type :get}
	      {:name daily-trends
	       :path ["trends" "daily"]
	       :params [:date :exclude]
	       :type :get}
	      {:name weekly-trends
	       :path ["trends" "weekly"]
	       :params [:date :exclude]
	       :type :get}]
    :parser rd
    :encoder ec
    :extension ".json?"})

  (api-builder/build-api
   {:root "twitter.com"
    :methods [{:name pub-timeline
	       :path ["statuses" "public_timeline"]
	       :params []
	       :type :get}
	      {:name friends-timeline
	       :path ["statuses" "friends_timeline"]
	       :params [:since_id :max_id :count :page]
	       :type :get}
	      {:name user-timeline
	       :path ["statuses" "user_timeline"]
	       :params [  :id :user_id :screen_name   :since_id :max_id :count :page]
	       :type :get}
	      {:name mentions
	       :path ["statuses" "mentions"]
	       :params [:since_id :max_id :count :page]
	       :type :get}
	      {:name id-status
	       :path ["statuses" "show"]
	       :params [:id]
	       :type :get}
	      {:name update-status
	       :path ["statuses" "update"]
	       :params [:status :in_reply_to_status_id]
	       :type :post}
	      {:name destroy-status
	       :path ["statuses" "destroy"]
	       :params [:id]
	       :type :post}

	      {:name user
	       :path ["user" "show"]
	       :params [:id :user_id :screen_name]
	       :type :get}
	      {:name user-friends
	       :path ["statuses" "friends"]
	       :params [:id :user_id :screen_name :page]
	       :type :get}
	      {:name user-followers
	       :path ["statuses" "followers"]
	       :params [:id :user_id :screen_name :page]
	       :type :get}

	      {:name messages
	       :path ["direct_messages"]
	       :params [  :since_id :max_id :count :page]
	       :type :get}
	      {:name sent-messages
	       :path ["direct_messages" "sent"]
	       :params [:since_id :max_id :count :page]
	       :type :get}
	      {:name send-message
	       :path ["direct_messages" "new"]
	       :params [:user :screen_name :user_id :text]
	       :type :post}
	      {:name direct-messages
	       :path ["direct_messages" "destroy"]
	       :params [:id]
	       :type :post}
	    
	      {:name add-friend
	       :path ["friendships" "create"]
	       :params [:id :user_id :screen_name :follow]
	       :type :post}
	      {:name remove-friend
	       :path ["friendships" "destroy"]
	       :params [:id :user_id :screen_name]
	       :type :post}
	      {:name friends?
	       :path ["friendships" "exists"]
	       :params [:user_a :user_b]
	       :type :get}
	      {:name show-friendship
	       :path ["friendships" "show"]
	       :params [  :source_id :source_screen_name :target_id :target_screen_name]
	       :type :get}

	      {:name friends-ids
	       :path ["friends" "ids"]
	       :params [  :id :user_id :screen_name :page]
	       :type :get}
	      {:name followers-ids
	       :path ["friends" "ids"]
	       :params [  :id :user_id :screen_name :page]
	       :type :get}

	      {:name verify-credentials
	       :path ["account" "verify_credentials"]
	       :params []
	       :type :get}
	      {:name rate-limit-status
	       :path ["account" "rate_limit_status"]
	       :params []
	       :type :get}
	      {:name end-session
	       :path ["account" "end_session"]
	       :params []
	       :type :post}


	      {:name update-delivery-device
	       :path ["account" "update_deliver_device"]
	       :params [:device]
	       :type :post}
	      {:name update-profile-colors
	       :path ["account" "update-profile-colors"]
	       :params [:profile_background_color
			:profile_text_color
			:profile_link_color
			:profile_sidebar_fill_color
			:profile_sidebar_border_color]
	       :type :post}
	      {:name update-profile-image
	       :path ["account" "update_profile_image"]
	       :params [:image]
	       :type :post
	       :send-type :image}
	      {:name update-profile-background-image
	       :path ["account" "update_profile_background_image"]
	       :params [:image :tile]
	       :type :post
	       :send-type :image}
	      {:name update-profile
	       :path ["account" "update-profile"]
	       :params [:name :email :url :location :description]
	       :type :post}

	      {:name favorites
	       :path ["favorites"]
	       :params [:id :page]
	       :type :get}
	      {:name add-favorite
	       :path ["favorites" "create"]
	       :params [:id]
	       :type :post}
	      {:name remove-favorite
	       :path ["favorites" "destroy"]
	       :params [:id]
	       :type :post}

	      {:name follow
	       :path ["notifications" "follow"]
	       :params [:id :user_id :screen_name]
	       :type :post}
	      {:name nofollow
	       :path ["notifications" "leave"]
	       :params [:id :user_id :screen_name]
	       :type :post}
	      
	      {:name block
	       :path ["blocks" "create"]
	       :params [:id]
	       :type :post}
	      {:name remove-block
	       :path ["blocks" "destroy"]
	       :params [:id]
	       :type :post}
	      {:name blocked?
	       :path ["blocks" "exists"]
	       :params [:id :user_id :screen_name]
	       :type :get}
	      {:name blocked-users
	       :path ["blocks" "blocking"]
	       :params [:page]
	       :type :get}
	      {:name blocked-ids
	       :path ["blocks" "blocking" "ids"]
	       :params [  :page]
	       :type :get}
	      
	      {:name saved-searches
	       :path ["saved_searches"]
	       :params []
	       :type :get}
	      {:name show-search
	       :path ["saved_searches" "show"]
	       :params [ :id]
	       :type :get}
	      {:name save-search
	       :path ["saved_searches" "create"]
	       :params [:query]
	       :type :post}
	      {:name delete-search
	       :path ["saved_searches" "destroy"]
	       :params [:id]
	       :type :post}]
    :parser rd
    :encoder ec
    :oauth true
    :extension ".json?"}))

(comment 
;; to be put into an init function or file
;; or can be put right here...

(twitter-api/make-request-fns {:con-key "fill this in"
					:con-secret "fill this in"
					:sig-method oauth.signpost.signature.SignatureMethod/HMAC_SHA1
					:request-token-url "http://twitter.com/oauth/request_token"
					:access-token-url "http://twitter.com/oauth/access_token"
					:auth-site-url "http://twitter.com/oauth/authorize"}))
