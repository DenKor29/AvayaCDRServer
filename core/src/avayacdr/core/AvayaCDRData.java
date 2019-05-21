package avayacdr.core;


public class AvayaCDRData extends BaseCDRData {

    public String cond_code;
    public String code_used;
    public String code_dial;
    public String in_trk_code;
    public String acct_code;
    public String auth_code;
    public String frl;
    public String ixc_code;
    public String in_crt_id;
    public String out_crt_id;
    public String feat_flag;
    public String code_return;
    public String line_feed;

    private static final String DATATIMEFORMAT = "ddMMyy HHmm";

    public void AvayCDRData()
    {
        SetPropertyCDR("avaya","");
    }

    @Override
    public void SetPropertyCDR(String name, String value) {

        String localValue = value.trim();

        if (!name.startsWith("avaya")) return;
        if (localValue.length() < 70) return ;

        super.SetPropertyCDR(name, localValue);
        SetFieldCDRTime(localValue,DATATIMEFORMAT,0,11);
        this.duration = GetFieldCDRInt(localValue,12,16,0);
        this.cond_code = GetFieldCDR(localValue,17,18);
        this.code_dial = GetFieldCDR(localValue,19,22);
        this.code_used = GetFieldCDR(localValue,23,26);
        this.in_trk_code = GetFieldCDR(localValue,27,30);
        this.callingNumber = GetFieldCDR(localValue,31,49);
        this.calledNumber = GetFieldCDR(localValue,50,65);
        this.acct_code = GetFieldCDR(localValue,66,71);
        this.auth_code = GetFieldCDR(localValue,72,77);
        this.frl = GetFieldCDR(localValue,78,79);
        this.ixc_code = GetFieldCDR(localValue,80,81);
        this.in_crt_id = GetFieldCDR(localValue,82,85);
        this.out_crt_id = GetFieldCDR(localValue,86,89);
        this.feat_flag = GetFieldCDR(localValue,90,91);
        this.code_return = GetFieldCDR(localValue,92,93);
        this.line_feed = GetFieldCDR(localValue,93,94);

    }
}
