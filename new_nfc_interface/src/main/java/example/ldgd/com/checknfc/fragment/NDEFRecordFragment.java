/*
  * @author STMicroelectronics MMY Application team
  *
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2017 STMicroelectronics</center></h2>
  *
  * Licensed under ST MIX_MYLIBERTY SOFTWARE LICENSE AGREEMENT (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  *        http://www.st.com/Mix_MyLiberty
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  * AND SPECIFICALLY DISCLAIMING THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  ******************************************************************************
*/

package example.ldgd.com.checknfc.fragment;

public abstract class NDEFRecordFragment extends STFragment {

    final static String TAG = "NDEFRecordFragment";

    // Key used when passing a NDEFMsg
    public static String NDEFKey = "NDEFKey";

    // Key used when passing a record number
    public static String RecordNbrKey = "RecordNbr";

    /**
     * Function used to request the fragment to update the NDEF record with the data
     * typed by the user.
     */
    public void updateContent() { return;}

    /**
     * Function to inform the fragment if the the NDEF Record can be edited or not
     * @param editable
     */
    public abstract void ndefRecordEditable(boolean editable);
}


