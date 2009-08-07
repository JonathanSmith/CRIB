; Copyright 2009 Jonathan A. Smith
; Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
; You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to
; in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
; either express or implied. See the License for the specific language governing permissions and limitations under the License. 

(ns api-builder)
;{:root :methods [{:name :path :params :type}*] :parser :encoder :extension :oauth-params}
(defn parameters-list-fn [keys encoder]
    (let [vars (map #(gensym (name %1)) keys)
	  kvs  (apply hash-map 
		      (interleave keys (map (fn key=val [key var]
					      (if encoder
						`(fn [array-list# ~var] 
						   (.add array-list# 
							 (new org.apache.http.message.BasicNameValuePair
							      ~(name key) (~encoder ~var))))
						`(fn [array-list# ~var] 
						   (.add array-list# 
							 (new org.apache.http.message.BasicNameValuePair
							      ~(name key) ~var)))))
					    keys vars)))]
      `(fn [hashmap#]
	 (let [array-list# (new java.util.ArrayList)
	       fn-hash# ~kvs]
	   (doseq [key# (keys hashmap#)]
	     (let [fun# (fn-hash# key#)]
	       (fun# array-list# (hashmap# key#))))
	   array-list#))))

(defn make-post-request-fn
  ([url-string keys send-type receive-type parser encoder]
     `(fn [params#]
	( ~(if parser parser 'clojure.core/identity)
	  (let [params-fn# ~(parameters-list-fn keys false)
		client# (new org.apache.http.impl.client.DefaultHttpClient)
		method# (new org.apache.http.client.methods.HttpPost (java.net.URI ~url-string))
		params-list# (params-fn#  params#)]

	    (.setEntity method# (new org.apache.http.client.entity.UrlEncodedFormEntity
				     params-list# org.apache.http.protocol.HTTP/UTF_8))
	    (.setBooleanParameter (.getParams method#) org.apache.http.params.CoreProtocolPNames/USE_EXPECT_CONTINUE false) 

	    (let [response#(.execute client# method#)
		  entity# (.getEntity response#)
		  in# (.getContent entity#)
		  rd# (new java.io.BufferedReader (new java.io.InputStreamReader  in#))]
	      (loop [line# (.readLine rd#)]
		(if-let [line-2# (.readLine rd#)]
		  (recur (str line# line-2#))
		  line#)))))))

  ([url-string keys send-type receive-type parser encoder oauth]
     `(fn [params#]
	( ~(if parser parser 'clojure.core/identity)
	  (let [params-fn# ~(parameters-list-fn keys false)
		client# (new org.apache.http.impl.client.DefaultHttpClient)
		method# (new org.apache.http.client.methods.HttpPost (new java.net.URI ~url-string))
		params-list# (params-fn# params#)]
	    
	    (.setEntity method# (new org.apache.http.client.entity.UrlEncodedFormEntity
				     params-list# org.apache.http.protocol.HTTP/UTF_8))
	    (.setBooleanParameter (.getParams method#) 
				  org.apache.http.params.CoreProtocolPNames/USE_EXPECT_CONTINUE false)
	    
	    
	    (~'signing-fn method#)
	    (let [response#(.execute client# method#)
		  entity# (.getEntity response#)
		  in# (.getContent entity#)
		  rd# (new java.io.BufferedReader (new java.io.InputStreamReader  in#))]
	       (.getProtocolVersion response#)
	      (.getStatusCode (.getStatusLine response#))
	      (.getReasonPhrase (.getStatusLine response#))
	       (.toString (.getStatusLine response#))
	      
	      (loop [line# (.readLine rd#)]
		(if-let [line-2# (.readLine rd#)]
		  (recur (str line# line-2#))
		  line#))))))))

(defn kv-fn [keys encoder]
  (let [vars (map #(gensym (name %1)) keys)
	kvs  (apply hash-map 
		    (interleave keys (map (fn key=val [key var]
					    (if encoder
					      `(fn [~var] 
						 (str ~(str (name key) "=") (~encoder ~var)))
					      `(fn [~var] 
						 (str ~(str (name key) "=") ~var))))
					  keys vars)))]
    `(fn [hashmap#]
       (let [fn-hash# ~kvs
	     key-vals# (map (fn [key#] 
			      (let [fun# (fn-hash# key#)]
				(fun# (hashmap# key#))))
			    (keys hashmap#))]
	 (if (== 0 (count key-vals#))
	   ""
	   (reduce (fn [x# y#] (str x# "&" y#)) key-vals#))))))

(defn make-get-request-fn 
  ([url-string keys receive-type parser encoder]   
     (cond
      (= receive-type :image)
      `(fn ([]   
	      (let [url# (new java.net.URL ~url-string)
		    image# (.. (java.awt.Toolkit/getDefaultToolkit) (createImage url#))]
		image#))

	 ([params#]   
	    (let [data-fn# ~(kv-fn keys encoder)
		  data#  (data-fn# params#)
		  url# (new java.net.URL (str ~url-string data#))
		  image# (.. (java.awt.Toolkit/getDefaultToolkit) (createImage url#))]
	      image#)))
      true
      `(fn ([]
	      (~(if parser parser 'clojure.core/identity)
	       (let [url# (new java.net.URL ~url-string)
		     conn# (.openConnection url#)
		     rd# (new java.io.BufferedReader (new java.io.InputStreamReader  (.getInputStream conn#)))]
		 (loop [line# (.readLine rd#)]
		   (if-let [line-2# (.readLine rd#)]
		     (recur (str line# line-2#))
		     line#)))))

	 ([params#]
	    ( ~(if parser parser 'clojure.core/identity)
	      (let [data-fn# ~(kv-fn keys encoder)
		    data# (data-fn# params#)
		    url# (new java.net.URL (str ~url-string data#))
		    conn# (.openConnection url#)
		    rd# (new java.io.BufferedReader (new java.io.InputStreamReader (.getInputStream conn#)))]
		(loop [line# (.readLine rd#)]
		  (if-let [line-2# (.readLine rd#)]
		    (recur (str line# line-2#))
		    line#))
		))))))

  ([url-string keys receive-type parser encoder oauth]
     (cond
      (= receive-type :image)
      `(fn ([]   
	      (let [client# (new org.apache.http.impl.client.DefaultHttpClient)
		    method# (new org.apache.http.client.methods.HttpGet (new java.net.URI  ~url-string))]
		(~'signing-fn method#)
		
		(let [response (.execute client# method#)
		      entity# (.getEntity response#)
		      bytes# (.. org.apache.http.util.EntityUtils (toByteArray entity#))
		      image# (.. (java.awt.Toolkit/getDefaultToolkit) (createImage bytes#))]

		  image#)))

	 ([params#]   
	    (let [data-fn# ~(kv-fn keys encoder)
		  data#  (data-fn# params#)
		  client# (new org.apache.http.impl.client.DefaultHttpClient)
		  method# (new org.apache.http.client.methods.HttpGet (new java.net.URI (str ~url-string data#)))]
	      (~'signing-fn method#)
	      
	      (let [response#(.execute client# method#)
		    entity# (.getEntity response#)
		    bytes# (.. org.apache.http.util.EntityUtils (toByteArray entity#))
		    image# (.. (java.awt.Toolkit/getDefaultToolkit) (createImage bytes#))]

		image#))))

      true
      `(fn ([](~(if parser parser 'clojure.core/identity)
	       (let [client# (new org.apache.http.impl.client.DefaultHttpClient)
		     method# (new org.apache.http.client.methods.HttpGet (new java.net.URI ~url-string))]
		 (~'signing-fn method#)
		 (let [response# (.execute client# method#)
		       entity# (.getEntity response#)
		       in# (.getContent entity#)
		       rd# (new java.io.BufferedReader(new java.io.InputStreamReader  in#))]
		   (loop [line# (.readLine rd#)]
		     (if-let [line-2# (.readLine rd#)]
		       (recur (str line# line-2#))
		       line#))))))

	 ([params#]
	    ( ~(if parser parser 'clojure.core/identity)
	      (let [data-fn# ~(kv-fn keys encoder)
		    data# (data-fn# params#)
		    client# (new org.apache.http.impl.client.DefaultHttpClient)
		    method# (new org.apache.http.client.methods.HttpGet (new java.net.URI (str ~url-string data#)))]
		(~'signing-fn method#)
		(let [response# (.execute client# method#)
		      entity# (.getEntity response#)
		      in# (.getContent entity#)
		      rd# (new java.io.BufferedReader (new java.io.InputStreamReader  in#))]
		  (loop [line# (.readLine rd#)]
		    (if-let [line-2# (.readLine rd#)]
		      (recur (str line# line-2#))
		      line#))))))))))

(defn make-url-string [root path extension]
  (apply str `("http://" ~root "/" ~@(reduce (fn [x y] (str x"/" y)) path) ~extension)))

(defn build-api-method 
  ([root {name :name
	  path :path
	  params :params
	  request-type :type
	  send-type :send-type
	  receive-type :receive-type}
    parser  encoder extension]
     (let [url-string (make-url-string root path extension)]
       `(def ~name 
	     ~(cond 
	       (= request-type :post)
	       (make-post-request-fn url-string params send-type receive-type parser encoder)
	       (= request-type :get) 
	       (make-get-request-fn url-string params receive-type parser encoder)))))

  ([root {name :name
	  path :path
	  params :params
	  request-type :type
	  send-type :send-type
	  receive-type :receive-type}
    parser encoder extension oauth]
     
     (let [url-string (make-url-string root path extension)]
       `(def ~name 
	     ~(cond 
	       (= request-type :post)
	       (make-post-request-fn url-string params send-type receive-type parser encoder oauth)
	       (= request-type :get) 
	       (make-get-request-fn url-string params receive-type parser encoder oauth))))))

(defn build-api-fn [{root :root
		     methods :methods
		     parser :parser
		     encoder :encoder
		     extension :extension
		     oauth :oauth}]
  
  (if oauth
    `(do 
       (defn ~'make-request-fns [{con-key# :con-key
				  con-secret# :con-secret
				  sig-method# :sig-method
				  request-token-url# :request-token-url
				  access-token-url#  :access-token-url
				  auth-site-url#     :auth-site-url}]

	 (let [consumer# (new oauth.signpost.commonshttp.CommonsHttpOAuthConsumer con-key# con-secret# sig-method#)
	       provider# (new oauth.signpost.basic.DefaultOAuthProvider
			      consumer#
			      request-token-url#
			      access-token-url#
			      auth-site-url#)]

	    (defn ~'token-secret []
	      (.getTokenSecret consumer#))
	   

	    (defn ~'get-access-token []
	      (.getToken consumer#))

	    (defn ~'set-token-with-secret 
	      [access-token# token-secret#]
	      (.setTokenWithSecret consumer# access-token# token-secret#))
	   

	    (defn ~'signing-fn  [request#]
	      (.sign consumer# request#))


	    (defn ~'request-token []
	      (.. provider# (retrieveRequestToken nil)))

	   (defn ~'access-token [verification-code#]
		      (.. provider# (retrieveAccessToken verification-code#)))))

       ~@(map #(build-api-method root % parser encoder extension oauth) methods))

    `(do 
       ~@(map #(build-api-method root % parser encoder extension) methods))))

(defmacro build-api [desc]
  (build-api-fn desc))